package io.kirill.hellocats.streams

import cats.effect._
import fs2.Stream
import io.kirill.hellocats.utils.printing._

import scala.concurrent.duration._

object Throttling extends IOApp {


  def throttle[F[_]: Async, A](stream: Stream[F, A], time: FiniteDuration, count: Int): Stream[F, A] = {
    val ticks = Stream.every[F](time)
    stream.zip(ticks).scan[(Option[A], Int)]((None, count + 1)) {
      case (_, (n, true)) => (Some(n), 0)
      case ((_, c), (n, _)) => (Some(n), c + 1)
    }
      .filter(_._2 < count)
      .map(_._1)
      .unNone
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val stream = Stream.constant[IO, Int](1).scan1(_ + _).metered(400.millis)

    throttle(stream, 1.seconds, 1)
      .evalMap(e => putStr[IO](e.toString))
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
