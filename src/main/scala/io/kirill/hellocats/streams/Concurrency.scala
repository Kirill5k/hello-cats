package io.kirill.hellocats.streams

import cats.effect.{Concurrent, Timer}
import fs2.Stream
import fs2.concurrent.{Signal, SignallingRef}

import scala.concurrent.duration.FiniteDuration

object Concurrency {

  def stopAfter[F[_]: Concurrent: Timer, A](in: Stream[F, A], delay: FiniteDuration): Stream[F, A] = {
    def out(interrupter: SignallingRef[F, Boolean]): Stream[F, A] =
      in.interruptWhen(interrupter)

    def stop(interrupter: SignallingRef[F, Boolean]): Stream[F, Unit] =
      Stream.sleep_[F](delay) ++ Stream.eval(interrupter.set(true))

    Stream.eval(SignallingRef[F, Boolean](false)).flatMap { interrupter =>
      out(interrupter).concurrently(stop(interrupter))
    }
  }

  def stopAfterPipe[F[_]: Concurrent: Timer, A](delay: FiniteDuration): Stream[F, A] => Stream[F, A] = in => {
    def close(interrupter: SignallingRef[F, Boolean]): Stream[F, Unit] =
      Stream.sleep_[F](delay) ++ Stream.eval(interrupter.set(true))

    Stream.eval(SignallingRef[F, Boolean](false)).flatMap { interrupter =>
      in.interruptWhen(interrupter).concurrently(close(interrupter))
    }

  }
}
