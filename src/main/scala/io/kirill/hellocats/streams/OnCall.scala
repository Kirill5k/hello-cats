package io.kirill.hellocats.streams

import cats.effect.std.{Queue, Dispatcher}
import cats.effect.{Async, ExitCode, IO, IOApp}
import fs2.Stream

object OnCall extends IOApp {

  final case class Message(text: String)

  trait Consumer {
    def onMessage(cb: Message => Unit): Unit
    def onError(cb: Throwable => Unit): Unit
  }

  def messageStream[F[_]](consumer: Consumer)(implicit F: Async[F]): Stream[F, Message] =
    for {
      q <- Stream.eval(Queue.unbounded[F, Either[Throwable, Message]])
      _ <- Stream.resource(Dispatcher[F]).evalMap { dispatcher =>
        F.delay {
          consumer.onMessage(m => dispatcher.unsafeRunAndForget(q.offer(Right(m))))
          consumer.onError(e => dispatcher.unsafeRunAndForget(q.offer(Left(e))))
        }
      }
      m <- Stream.fromQueueUnterminated(q).rethrow
    } yield m

  override def run(args: List[String]): IO[ExitCode] = ???
}
