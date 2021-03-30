package io.kirill.hellocats.streams

import java.time.LocalTime
import cats.effect.{ExitCode, IO, IOApp, Temporal}
import fs2.Stream
import io.kirill.hellocats.utils.printing._

import scala.concurrent.duration._

object Metered extends IOApp.Simple {

  def fixedRateImmediate[F[_]](d: FiniteDuration)(implicit F: Temporal[F]): Stream[F, Unit] = {
    def now: Stream[F, Long] = Stream.eval(F.monotonic).map(_.toMillis)
    def go(started: Long): Stream[F, Unit] =
      now.flatMap { finished =>
        val elapsed = finished - started
        Stream.emit(()) ++ Stream.sleep_(d - elapsed.nanos) ++ now.flatMap { started =>
          go(started)
        }
      }
    now.flatMap(go)
  }

  val timeStream = Stream
    .repeatEval(putStr[IO](LocalTime.now))
    .evalTap(_ => IO(println("foo")))
    .metered(1.second)

  val immediateTimeStream = Stream
    .repeatEval(putStr[IO](LocalTime.now))
    .evalTap(_ => IO.sleep(1.second))

  val anotherImmediateStream = Stream
    .repeatEval(IO(LocalTime.now))
    .zipLeft(fixedRateImmediate[IO](1.second))
    .evalTap(t => putStr[IO](s"$t - tapping"))

  override def run: IO[Unit] =
   putStr[IO](s"${LocalTime.now()} starting stream") *>
     anotherImmediateStream.compile.drain

}
