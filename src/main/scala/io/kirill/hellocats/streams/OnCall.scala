package io.kirill.hellocats.streams

import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import fs2.Stream
import fs2.concurrent.Queue

object OnCall extends IOApp {

  final case class Message(text: String)

  trait Consumer {
    def onMessage(cb: Message => Unit): Unit
    def onError(cb: Throwable => Unit): Unit
  }

  def messageStream[F[_]](consumer: Consumer)(implicit F: ConcurrentEffect[F]): Stream[F, Message] =
    for {
      q <- Stream.eval(Queue.unbounded[F, Either[Throwable, Message]])
      _ <- Stream.eval(F.delay {
        consumer.onMessage(m => F.runAsync(q.enqueue1(Right(m)))(_ => IO.unit).unsafeRunSync())
        consumer.onError(e => F.runAsync(q.enqueue1(Left(e)))(_ => IO.unit).unsafeRunSync())
      })
      m <- q.dequeue.rethrow
    } yield m

  override def run(args: List[String]): IO[ExitCode] = ???
}
