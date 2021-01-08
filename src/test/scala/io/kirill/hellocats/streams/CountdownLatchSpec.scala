package io.kirill.hellocats.streams

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration._

class CountdownLatchSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A RefbasedCountdownLatch" - {

    "should release when count reaches 0" in {
      val res = for {
        countdownLatch <- CountdownLatch.make[IO](5)
        _              <- fs2.Stream.repeatEval(countdownLatch.decrement()).metered(100.millis).take(5).compile.drain.start
        _              <- countdownLatch.await()
      } yield ()

      res.asserting(_ mustBe ())
    }
  }
}
