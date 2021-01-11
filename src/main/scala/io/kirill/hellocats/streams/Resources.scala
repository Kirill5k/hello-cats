package io.kirill.hellocats.streams

import cats.effect.{ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits._
import fs2.Stream
import utils._

object Resources extends IOApp {

  trait Releasable[F[_]] {
    def release: F[Unit]
    def reportValue: F[Unit]
  }

  def syncCounter[F[_]](name: String)(implicit F: Sync[F]): F[Releasable[F]] =
    for {
      count <- F.delay(new java.util.concurrent.atomic.AtomicLong(0))
      _     <- putStr(s"$name acquired/incremented: ${count.incrementAndGet()}")
      releasable = new Releasable[F] {
        override def release: F[Unit]     = putStr[F](s"$name released/decremented: ${count.decrementAndGet}")
        override def reportValue: F[Unit] = putStr[F](s"$name current count is ${count.intValue()}")
      }
    } yield releasable

  def streamResource[F[_]: Sync]: Stream[F, Unit] =
    Stream
      .bracket(syncCounter[F]("stream"))(_.release)
      .evalMap(_.reportValue)

  def effectResource[F[_]: Sync]: Resource[F, Unit] =
    Resource
      .make(syncCounter[F]("effect"))(_.release)
      .evalMap(_.reportValue)

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- effectResource[IO].use(_ => putStr[IO]("foo"))
      _ <- streamResource[IO].compile.drain
    } yield ExitCode.Success
}
