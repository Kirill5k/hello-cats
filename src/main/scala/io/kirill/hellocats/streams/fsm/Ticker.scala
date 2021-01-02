package io.kirill.hellocats.streams.fsm

import scala.concurrent.duration._
import cats._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.Stream

trait Ticker[F[_]] {
  def get: F[Tick]
  def ticks: Stream[F, Tick]
}

object Ticker {

  def create[F[_]: Clock: Concurrent](
      maxNrOfEvents: Int,
      timeWindow: FiniteDuration
  ): F[Ticker[F]] =
    Ref.of[F, Tick](Tick.Off).map { tick =>
      new Ticker[F] {
        def get: F[Tick] = tick.get

        def ticks: Stream[F, Tick] = {
          val duration = timeWindow.toNanos
          val interval = (duration * 0.05).toLong

          Stream.unfoldLoopEval[F, (Long, Long), Tick]((0, 0)) { case (lastSpikeNanos, eventsCount) =>
            (Clock[F].monotonic(NANOSECONDS), get).tupled.map { case (now, tick) =>
              if ((now - lastSpikeNanos) > duration || (tick === Tick.On && (now - lastSpikeNanos) > interval) || eventsCount >= maxNrOfEvents)
                (Tick.On, Some(now, 0))
              else (Tick.Off, Some(lastSpikeNanos, eventsCount+1))
            }
          }.tail
        }

      }
    }
}

sealed trait Tick
object Tick {
  case object On  extends Tick
  case object Off extends Tick

  implicit val eq: Eq[Tick] = Eq.fromUniversalEquals

  implicit val semigroup: Semigroup[Tick] =
    new Semigroup[Tick] {
      def combine(x: Tick, y: Tick): Tick = (x, y) match {
        case (On, _)    => On
        case (_, On)    => On
        case (Off, Off) => Off
      }
    }
}
