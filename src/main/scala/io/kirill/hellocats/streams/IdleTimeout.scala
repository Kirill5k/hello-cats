package io.kirill.hellocats.streams

import cats.effect.{Concurrent, ExitCode, IO, IOApp, Timer}
import fs2.Stream

import scala.concurrent.duration.FiniteDuration

object IdleTimeout extends IOApp {

  def idleTimeout[F[_], A](
      s: fs2.Stream[F, A],
      timeout: FiniteDuration
  )(implicit
      F: Concurrent[F],
      timer: Timer[F]
  ): fs2.Stream[F, A] =
    s.groupWithin(1, timeout)
      .evalMap(
        _.head.fold[F[A]](F.raiseError(new Exception("timeout")))(F.pure)
      )

  override def run(args: List[String]): IO[ExitCode] = ???
}
