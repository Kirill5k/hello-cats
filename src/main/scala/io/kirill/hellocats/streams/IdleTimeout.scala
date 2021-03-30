package io.kirill.hellocats.streams

import cats.effect.implicits._
import cats.effect.kernel.Temporal
import cats.effect.{Deferred, Fiber, IO, IOApp}
import cats.implicits._
import fs2.Stream
import io.kirill.hellocats.utils.printing._

import scala.concurrent.duration._

object IdleTimeout extends IOApp.Simple {

  implicit final private class StreamOps[F[_], A](
      private val stream: Stream[F, A]
  ) {

    def idleTimeout(timeout: FiniteDuration)(implicit F: Temporal[F]): Stream[F, A] = {
      def timeoutTimer(stopSignal: Deferred[F, Either[Throwable, Unit]]): F[Unit] =
        F.sleep(timeout) *> stopSignal.complete(Left(new RuntimeException("timeout"))).void

      (for {
        stopSignal <- Stream.eval(Deferred[F, Either[Throwable, Unit]])
        startTimer <- Stream.eval(timeoutTimer(stopSignal).start)
        res <- stream
          .evalScan[F, (Fiber[F, Throwable, Unit], Option[A])]((startTimer, None)) { case ((to, _), el) =>
            to.cancel *> timeoutTimer(stopSignal).start.map(t => (t, Some(el)))
          }
          .interruptWhen(stopSignal)
      } yield res).map(_._2).unNone
    }
  }

  override def run: IO[Unit] =
    Stream
      .ranges[IO](0, 100, 1)
      .evalTap(i => putStr[IO](i.toString))
      .metered(1.second)
      .idleTimeout(500.millis)
      .handleErrorWith(e => Stream.eval(putStr[IO](e.getMessage)).drain)
      .compile
      .drain
}
