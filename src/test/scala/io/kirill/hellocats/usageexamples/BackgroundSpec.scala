package io.kirill.hellocats.usageexamples

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import fs2.Stream

import scala.concurrent.duration._

class BackgroundSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A Background should " - {

    "schedule and run multiple tasks in background" in {
      Background
        .resource[IO]
        .use { bg =>
          for {
            queue  <- Queue.unbounded[IO, String]
            _      <- bg.schedule(queue.offer("world"), 2.seconds)
            _      <- bg.schedule(queue.offer("hello"), 1.seconds)
            _      <- bg.schedule(queue.offer("!"), 3.seconds)
            result <- Stream.fromQueueUnterminated(queue).take(3).compile.toList
          } yield result
        }
        .map(_ mustBe List("hello", "world", "!"))
    }
  }
}
