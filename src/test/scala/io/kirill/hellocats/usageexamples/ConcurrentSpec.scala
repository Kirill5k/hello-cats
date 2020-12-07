package io.kirill.hellocats.usageexamples

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Timer}
import cats.implicits._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import sttp.client.Response
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.testing.SttpBackendStub
import sttp.model.Method

import scala.concurrent.duration._
import scala.language.postfixOps

class ConcurrentSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A Concurrent should" - {

    "race" - {
      "return the fastest to execute" in {
        val e1 = IO.sleep(3.seconds) *> IO.pure("1")
        val e2 = IO.sleep(1.seconds) *> IO.pure("2")

        IO.race(e1, e2).asserting(_ mustBe Right("2"))
      }

      "return error if it executes faster" in {
        val e1 = IO.sleep(3.seconds) *> IO.pure("1")
        val e2 = IO.sleep(1.seconds) *> IO.raiseError(new RuntimeException())

        IO.race(e1, e2).assertThrows[RuntimeException]
      }
    }
  }
}
