package io.kirill.hellocats.streams

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import fs2.Stream

class GroupingSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A groupBy should" - {

    "group stream elements into multiple substreams" in {
      val input = Stream.range(1, 11).covary[IO]

      val grouped = input
        .through(Grouping.groupBy(i => IO(i % 2)))
        .compile
        .toList

      grouped
        .flatMap { g =>
          g.traverse { case (k, stream) => stream.compile.toList.map(v => (k, v)) }
        }
        .map { res =>
          res mustBe List((1, List(1, 3, 5, 7, 9)), (0, List(2, 4, 6, 8, 10)))
        }
    }
  }
}
