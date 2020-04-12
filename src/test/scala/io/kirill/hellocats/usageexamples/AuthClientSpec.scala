package io.kirill.hellocats.usageexamples

import cats.effect.{IO, Timer}
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

import scala.concurrent.duration._
import scala.language.postfixOps

class AuthClientSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  import AuthClient._
  implicit val c: Config = Config("http://localhost:8080", "client-id", "client-secret")

  "An AuthClient should" - {

    "authenticate on initialisation and return access token" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatches(_.method == Method.POST)
        .thenRespondCyclicResponses(
          Response.ok[String](authResponse("token-1", 3600)),
          Response.ok[String](authResponse("token-2", 3600))
        )

      val authClient = new AuthClient[IO]()

      val token = authClient.token

      token.asserting(_ must be ("token-1"))
    }

    "renew access token when it expires" in {
      implicit val testingBackend: SttpBackendStub[IO, Nothing] = AsyncHttpClientCatsBackend.stub[IO]
        .whenRequestMatches(_.method == Method.POST)
        .thenRespondCyclicResponses(
          Response.ok[String](authResponse("token-1", 5)),
          Response.ok[String](authResponse("token-2", 3600))
        )

      val authClient = new AuthClient[IO]()

      val token = Timer[IO].sleep(8 seconds) *> authClient.token

      token.asserting(_ must be ("token-2"))
    }
  }

  def authResponse(token: String, expiresIn: Int): String =
    s"""{"access_token":"$token","expires_in":"$expiresIn"}"""
}
