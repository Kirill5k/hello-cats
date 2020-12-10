package io.kirill.hellocats.streams

import cats.effect.concurrent.MVar
import cats.effect.{Concurrent, Timer}
import cats.implicits._
import fs2.{Pipe, Stream}
import fs2.concurrent.SignallingRef

import scala.concurrent.duration._

object FlowControl {

  def stopAfter[F[_]: Concurrent: Timer, A](in: Stream[F, A], delay: FiniteDuration): Stream[F, A] = {
    def out(interrupter: SignallingRef[F, Boolean]): Stream[F, A] =
      in.interruptWhen(interrupter)

    def stop(interrupter: SignallingRef[F, Boolean]): Stream[F, Unit] =
      Stream.sleep_[F](delay) ++ Stream.eval(interrupter.set(true))

    Stream.eval(SignallingRef[F, Boolean](false)).flatMap { interrupter =>
      out(interrupter).concurrently(stop(interrupter))
    }
  }

  def stopAfterPipe[F[_]: Concurrent: Timer, A](delay: FiniteDuration): Pipe[F, A, A] = in => {
    def close(interrupter: SignallingRef[F, Boolean]): Stream[F, Unit] =
      Stream.sleep_[F](delay) ++ Stream.eval(interrupter.set(true))

    Stream.eval(SignallingRef[F, Boolean](false)).flatMap { interrupter =>
      in.interruptWhen(interrupter).concurrently(close(interrupter))
    }
  }

  /**
   * val slowDown = slowDownEveryN(
   *    resets = Stream.awakeEvery[IO](3.seconds).void
   *    n = 5
   * )
   * clientMessages.zipLeft(slowDown)
   */
  def slowDownEveryN[F[_]: Timer](resets: Stream[F, Unit], n: Int)(implicit F: Concurrent[F]): Stream[F, FiniteDuration] = {
    val slowingDown       = Stream.eval_(F.delay(println("----- Slowing down -----")))
    val resetting         = F.delay(println("----- Resetting delays! -----"))
    val delaysExponential = Stream.iterate(1.milli)(_ * 2).flatMap(Stream.awakeDelay[F](_).take(n.toLong) ++ slowingDown)

    Stream.eval(MVar.empty[F, Unit]).flatMap { restart =>
      val delaysUntilReset = delaysExponential.interruptWhen(restart.take.attempt)

      delaysUntilReset.repeat.concurrently(resets.evalMap(_ => restart.put(()) *> resetting))
    }
  }

  def tk[F[_],O](n: Long): Pipe[F,O,O] =
    in => in.scanChunksOpt(n) { n =>
      if (n <= 0) None
      else Some(c => c.size match {
        case m if m < n => (n - m, c)
        case m => (0, c.take(n.toInt))
      })
    }

}
