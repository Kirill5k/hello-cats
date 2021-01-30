package io.kirill.hellocats.effects

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

class QueueSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "An UnboundedQueue should" - {

    "enqueue and dequeue elements in first in first out order" in {
      val res = for {
        queue <- Queue.unbounded[IO, String]
        _     <- queue.enqueue("e1")
        _     <- queue.enqueue("e2")
        e1    <- queue.dequeue
        e2    <- queue.dequeue
      } yield (e1, e2)

      res.asserting(_ mustBe ("e1", "e2"))
    }
  }
}
