package io.kirill.hellocats.usageexamples

import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import cats.implicits._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.hellocats.usageexamples.AuthClient.Config
import sttp.client.{NothingT, SttpBackend}
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

import scala.concurrent.duration._
import scala.language.postfixOps

class AuthClient[F[_]: Concurrent: Timer](implicit c: Config, b: SttpBackend[F, Nothing, NothingT]) {
  import AuthClient._

  private def renew(ref: Ref[F, AuthResponse]): F[Unit] = {
    for {
      _ <- Concurrent[F].delay(println("renewing"))
      as <- ref.get
      _ <- Timer[F].sleep(as.expires_in seconds)
      _ <- authenticate().flatTap(ref.set)
      _ <- renew(ref)
    } yield ()
  }

  private val authToken: F[Ref[F, AuthResponse]] =
    authenticate()
      .flatMap(res => Ref.of[F, AuthResponse](res))
      .flatTap(ref => Concurrent[F].start(renew(ref)).void)

  def token: F[String] = authToken.flatMap(_.get).map(_.access_token)

  private def authenticate(): F[AuthResponse] =
    Concurrent[F].delay(println("authenticating")) *>
    basicRequest
      .body(Map("grant_type" -> "authorization_code"))
      .auth.basic(c.clientId, c.clientSecret)
      .contentType(MediaType.ApplicationXWwwFormUrlencoded)
      .post(uri"${c.baseUrl}/auth/token")
      .response(asJson[AuthResponse])
      .send()
      .flatMap { r =>
        r.body match {
          case Right(success) =>
            Sync[F].pure(success)
          case Left(errorResponse) =>
            val error: Either[Throwable, AuthResponse] =
              decode[AuthError](errorResponse.body)
                .flatMap(e => Left(ApiError(s"error ${r.code.code}: ${e.error_description}")))
            Sync[F].fromEither(error)
        }
      }
}

object AuthClient {
  final case class Config(baseUrl: String, clientId: String, clientSecret: String)

  final case class AuthResponse(access_token: String, expires_in: Int)
  final case class AuthError(error: String, error_description: String)

  final case class ApiError(message: String) extends Throwable
}
