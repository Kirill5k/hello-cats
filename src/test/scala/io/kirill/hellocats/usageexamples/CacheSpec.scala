package io.kirill.hellocats.usageexamples

import cats.effect._
import cats.implicits._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration._

class CacheSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A RefCache" - {

    "should store value in cache" in {
      val cache = CacheF.of[IO, String, String](10.seconds, 2.seconds)

      val result = cache.flatTap(_.put("key", "value")).flatMap(_.get("key"))

      result.asserting(_ mustBe (Some("value")))
    }


    "should check if value exists" in {
      val cache = CacheF.of[IO, String, String](10.seconds, 2.seconds)

      val result = cache.flatTap(_.put("key", "value")).flatMap(_.exists("key"))

      result.asserting(_ mustBe (true))
    }


    "should clear value if it has Expired" in {
      val cache = CacheF.of[IO, String, String](1.seconds, 2.seconds)

      val result = cache.flatTap(_.put("key", "value")).flatTap(_ => IO.sleep(3.seconds)).flatMap(_.get("key"))

      result.asserting(_ mustBe (None))
    }
  }
}
