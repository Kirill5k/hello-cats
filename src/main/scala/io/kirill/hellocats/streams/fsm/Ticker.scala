package io.kirill.hellocats.streams.fsm

import scala.concurrent.duration._
import Ticker.Count
import cats._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.Stream

import java.time.Instant

trait Ticker[F[_]] {
  def get: F[Tick]
  def merge(timerTick: Tick, count: Count): F[(Tick, Count)]
  def ticks: Stream[F, Tick]
}

object Ticker {
  type Count = Int

  def create[F[_]: Clock: Concurrent](
      maxNrOfEvents: Int,
      timeWindow: FiniteDuration
  ): F[Ticker[F]] =
    Ref.of[F, Tick](Tick.Off).map { ref =>
      new Ticker[F] {
        def get: F[Tick] = ref.get

        def merge(timerTick: Tick, count: Count): F[(Tick, Count)] =
          ref
            .modify {
              case Tick.Off if count === maxNrOfEvents => Tick.On  -> 0
              case _ if timerTick === Tick.On          => Tick.Off -> 0
              case _                                   => Tick.Off -> (count + 1)
            }
            .flatMap { newCount =>
              get.map { counterTick =>
                val newTick = counterTick |+| timerTick
                newTick -> newCount
              }
            }

        def ticks: Stream[F, Tick] = {
          val duration = timeWindow.toNanos
          val interval = FiniteDuration((timeWindow.toSeconds * 0.05).toLong, SECONDS).toNanos

          def go(lastSpikeNanos: Long): Stream[F, Tick] =
            Stream.eval((Concurrent[F].delay(Instant.now.getNano), get).tupled).flatMap { case (now, tick) =>
              if ((now - lastSpikeNanos) > duration || (tick === Tick.On && (now - lastSpikeNanos) > interval))
                Stream.emit(Tick.On) ++ go(now)
              else Stream.emit(Tick.Off) ++ go(lastSpikeNanos)
            }

          go(0).tail
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
