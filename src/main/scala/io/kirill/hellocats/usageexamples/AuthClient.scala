package io.kirill.hellocats.usageexamples

import cats.effect.Sync
import cats.implicits._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.kirill.hellocats.usageexamples.AuthClient.Config
import sttp.client.{NothingT, SttpBackend}
import sttp.client._
import sttp.client.circe._
import sttp.model.MediaType

class AuthClient[F[_]: Sync](implicit c: Config, b: SttpBackend[F, Nothing, NothingT]) {
  import AuthClient._

  private def authenticate(): F[AuthResponse] =
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
            val error: Either[Throwable, AuthResponse] = decode[AuthError](errorResponse.body).flatMap(e => Left(ApiError(s"error ${r.code.code}: ${e.error_description}")))
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
