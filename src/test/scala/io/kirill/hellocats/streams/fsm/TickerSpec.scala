package io.kirill.hellocats.streams.fsm

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration._

class TickerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A Ticker should" - {

    "produce ticks after time window has elapsed" in {
      val res = for {
        ticker <- Ticker.create[IO](100000, 1.seconds)
        ticks <- ticker.ticks.interruptAfter(3.seconds).compile.toList
      } yield ticks

      res.asserting { ticks =>
        ticks.filter(_ == Tick.On) must have size 4
      }
    }

    "produce ticks after certain amount of events" in {
      val res = for {
        ticker <- Ticker.create[IO](10, 3.seconds)
        ticks <- ticker.ticks.take(1000).compile.toList
      } yield ticks

      res.asserting { ticks =>
        ticks.filter(_ == Tick.On) must have size 91
      }
    }
  }
}
