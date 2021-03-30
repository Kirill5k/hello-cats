package io.kirill.hellocats.streams

import cats.effect.kernel.Ref
import cats.effect.{ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits._
import fs2.Stream
import io.kirill.hellocats.utils.printing._

object Resources extends IOApp {

  trait Releasable[F[_]] {
    def release: F[Unit]
    def reportValue: F[Unit]
  }

  def syncCounter[F[_]](name: String)(implicit F: Sync[F]): F[Releasable[F]] =
    for {
      count <- Ref.of[F, Int](0)
      _     <- count.updateAndGet(_ + 1).flatMap(c => putStr(s"$name acquired/incremented: $c"))
      releasable = new Releasable[F] {
        override def release: F[Unit]     = count.updateAndGet(_ - 1).flatMap(c => putStr[F](s"$name released/decremented: $c"))
        override def reportValue: F[Unit] = count.get.flatMap(c => putStr[F](s"$name current count is $c"))
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
