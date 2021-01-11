package io.kirill.hellocats.streams

import cats.effect.{ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits._
import fs2.Stream

object Resources extends IOApp {

  trait Releasable[F[_], A] {
    def release: F[Unit]
    def get: F[A]
  }

  def syncCounter[F[_]: Sync] = new Releasable[F, Int] {
    private val count = new java.util.concurrent.atomic.AtomicLong(0)

    override def acquire: F[Unit] =
      Sync[F].delay { println(s"acquired/incremented: ${count.incrementAndGet}"); count.intValue() }

    override def release: F[Unit] =
      Sync[F].delay { println(s"released/decremented: ${count.decrementAndGet}"); () }

    override def get: F[Int] = Sync[F].delay(count.intValue())
  }


  def streamResource[F[_]: Sync]: Stream[F, Unit] =
    Stream.emit(syncCounter[F])
    .flatMap { c =>
      Stream
        .bracket(c.acquire)(_ => c.release)
        .evalMap(_ => c.get.flatMap(count => Sync[F].delay(println(s"current count is $count"))))
    }

  def syncResource[F[_]: Sync]: Resource[F, Unit] =
    Resource.liftF(Sync[F].delay(syncCounter[F])).flatMap { c =>
      Resource
        .make(c.acquire)(_ => c.release)
        .evalMap(_ => c.get.flatMap(count => Sync[F].delay(println(s"current count is $count"))))
    }

  override def run(args: List[String]): IO[ExitCode] = {
    syncResource[IO].use { _ =>
      IO(println("foo"))
    }.as(ExitCode.Success)
  }
}
