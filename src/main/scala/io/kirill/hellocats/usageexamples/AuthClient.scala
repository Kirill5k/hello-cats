package io.kirill.hellocats.usageexamples

import cats.effect.{Clock, Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import cats.implicits._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.hellocats.usageexamples.AuthClient.{ApiError, AuthError, AuthResponse, Config}
import sttp.client.{NothingT, SttpBackend}
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

import scala.concurrent.duration._
import scala.language.postfixOps

object AuthApi {
  def authenticate[F[_]: Sync](implicit c: Config, b: SttpBackend[F, Nothing, NothingT]): F[AuthResponse] =
    Sync[F].delay(println("authenticating")) *>
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


class AuthClient[F[_]: Sync](state: Ref[F, AuthResponse]) {
  def token: F[String] = state.get.map(_.access_token)
}

object AuthClient {
  final case class Config(baseUrl: String, clientId: String, clientSecret: String)

  final case class AuthResponse(access_token: String, expires_in: Int)
  final case class AuthError(error: String, error_description: String)

  final case class ApiError(message: String) extends Throwable

  def authClient[F[_]: Concurrent](implicit c: Config, b: SttpBackend[F, Nothing, NothingT], t: Timer[F]): F[AuthClient[F]] = {
    def renew(ref: Ref[F, AuthResponse]): F[Unit] =
      for {
        _ <- Concurrent[F].delay(println("renewing"))
        as <- ref.get
        _ <- t.sleep(as.expires_in seconds)
        _ <- AuthApi.authenticate.flatTap(ref.set)
        _ <- renew(ref)
      } yield ()

    Ref.of[F, AuthResponse](AuthResponse("foo", 0))
      .flatTap(ref => Concurrent[F].start(renew(ref)).void)
      .map(ref => new AuthClient[F](ref))
  }
}
