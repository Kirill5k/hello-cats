package io.kirill.hellocats.streams

import cats.effect.{ExitCode, IO, IOApp, Sync}
import fs2.Stream

object Resources extends IOApp {

  val count = new java.util.concurrent.atomic.AtomicLong(0)
  def acquire[F[_]: Sync]: F[Int] =
    Sync[F].delay { println(s"incremented: ${count.incrementAndGet}"); count.intValue() }
  def release[F[_]: Sync]: F[Unit] =
    Sync[F].delay { println(s"decremented: ${count.decrementAndGet}"); () }

  override def run(args: List[String]): IO[ExitCode] =
    Stream
      .bracket(acquire[IO])(_ => release[IO])
      .evalMap(c => IO(println(s"current $c")))
      .compile
      .drain
      .as(ExitCode.Success)
}
