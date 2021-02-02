package io.kirill.hellocats.streams

import cats.effect.concurrent.Deferred
import cats.effect.{Concurrent, ExitCode, Fiber, IO, IOApp, Timer}
import cats.implicits._
import cats.effect.implicits._
import fs2.Stream
import fs2.concurrent.SignallingRef
import io.kirill.hellocats.utils.printing._

import scala.concurrent.duration._

object IdleTimeout extends IOApp {

  implicit final private class StreamOps[F[_], A](
      private val stream: Stream[F, A]
  ) {

    def idleTimeout(timeout: FiniteDuration)(implicit T: Timer[F], F: Concurrent[F]): Stream[F, A] = {
      def timeoutTimer(stopSignal: Deferred[F, Either[Throwable, Unit]]): F[Unit] =
        T.sleep(timeout) *> stopSignal.complete(Left(new RuntimeException("timeout")))

      (for {
        stopSignal <- Stream.eval(Deferred[F, Either[Throwable, Unit]])
        startTimer <- Stream.eval(timeoutTimer(stopSignal).start)
        res <- stream
          .evalScan[F, (Fiber[F, Unit], Option[A])]((startTimer, None)) { case ((to, _), el) =>
            to.cancel *> timeoutTimer(stopSignal).start.map(t => (t, Some(el)))
          }
          .interruptWhen(stopSignal)
      } yield res).map(_._2).unNone
    }
  }

  override def run(args: List[String]): IO[ExitCode] =
    Stream
      .range[IO](0, 100)
      .evalTap(i => putStr[IO](i.toString))
      .metered(1.second)
      .idleTimeout(500.millis)
      .handleErrorWith(e => Stream.eval_(putStr[IO](e.getMessage)))
      .compile
      .drain
      .as(ExitCode.Success)
}
