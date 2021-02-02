package io.kirill.hellocats.streams

import cats.effect.{Concurrent, ExitCode, IO, IOApp, Timer}
import fs2.Stream
import io.kirill.hellocats.utils.printing._

import scala.concurrent.duration._

object IdleTimeout extends IOApp {

  def idleTimeout[F[_]: Timer, A](
      s: fs2.Stream[F, A],
      timeout: FiniteDuration
  )(implicit F: Concurrent[F]): fs2.Stream[F, A] =
    s
      .groupWithin(1, timeout)
      .evalMap(_.head.fold[F[A]](F.raiseError(new Exception("timeout")))(F.pure))

  override def run(args: List[String]): IO[ExitCode] = {
    val s = Stream
      .range[IO](0, 100)
      .evalTap(i => putStr[IO](i.toString))
      .metered(1.second)
    idleTimeout[IO, Int](s, 100.millis)
      .handleErrorWith(e => Stream.eval_(putStr[IO](e.getMessage)))
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
