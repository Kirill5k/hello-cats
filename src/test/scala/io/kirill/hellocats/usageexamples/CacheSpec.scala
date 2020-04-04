package io.kirill.hellocats.usageexamples

import cats.effect._
import cats.implicits._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration._
import scala.language.postfixOps

class CacheSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A RefCache" - {

    "should store value in cache" in {
      val cache = Cache.refCache[IO, String, String](10 seconds, 2 seconds)

      val result = cache.put("key", "value") *> cache.get("key")

      result.asserting(_ must be (Some("value")))
    }
  }
}
