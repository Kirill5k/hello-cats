package io.kirill.hellocats.streams

import cats.effect.{ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits._
import fs2.Stream
import utils._

object Resources extends IOApp {

  trait Releasable[F[_], A] {
    def release: F[Unit]
    def value: F[A]
  }

  def syncCounter[F[_]](implicit F: Sync[F]): F[Releasable[F, Int]] =
    for {
      count <- F.delay(new java.util.concurrent.atomic.AtomicLong(0))
      _     <- F.delay { println(s"acquired/incremented: ${count.incrementAndGet}"); count.intValue() }
      releasable = new Releasable[F, Int] {
        override def release: F[Unit] = F.delay { println(s"released/decremented: ${count.decrementAndGet}"); () }
        override def value: F[Int]      = F.delay(count.intValue())
      }
    } yield releasable

  def streamResource[F[_]: Sync]: Stream[F, Unit] =
    Stream
      .bracket(syncCounter[F])(_.release)
      .evalMap(_.value.flatMap(count => Sync[F].delay(println(s"current count is $count"))))

  def effectResource[F[_]: Sync]: Resource[F, Unit] =
    Resource
      .make(syncCounter[F])(_.release)
      .evalMap(_.value.flatMap(count => Sync[F].delay(println(s"current count is $count"))))

  override def run(args: List[String]): IO[ExitCode] =
    effectResource[IO]
      .use { _ =>
        IO(println("foo"))
      }
      .as(ExitCode.Success)
}
