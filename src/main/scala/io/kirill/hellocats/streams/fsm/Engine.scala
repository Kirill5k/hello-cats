package io.kirill.hellocats.streams.fsm

import cats.{Applicative, Parallel}
import cats.effect._
import cats.implicits._
import io.kirill.hellocats.streams.fsm.game._
import fs2.Pipe

final case class Engine[F[_]: Concurrent: Parallel: Time: Timer](
    publish: Summary => F[Unit],
    ticker: Ticker[F]
) {
  private val fsm = Engine.fsm[F](ticker)

  def run: Pipe[F, Event, Unit] =
    _.noneTerminate
      .zip(ticker.ticks)
      .evalMapAccumulate(Map.empty[PlayerId, Agg] -> 0)(fsm.run)
      .collect { case (_, (out, Tick.On)) => out }
      .evalMap { m =>
        Time[F].now.flatMap { ts =>
          m.toList.parTraverse_ { case (pid, agg) =>
            publish(agg.summary(pid, ts))
          }
        }
      }
}

object Engine {
  type Input  = (Option[Event], Tick)
  type Output = (Map[PlayerId, Agg], Tick)
  type State  = (Map[PlayerId, Agg], Int)

  def fsm[F[_]: Applicative](ticker: Ticker[F]): FSM[F, State, Input, Output] = new FSM[F, State, Input, Output] {
    override def run(state: State, input: Input): F[(State, Output)] =
      (state, input) match {
        case ((aggs, count), (Some(event), tick)) =>
          val (playerId, modifier) = event match {
            case Event.LevelUp(pid, level, _) =>
              pid -> Agg._Points.modify(_ + 100).andThen(Agg._Level.set(level))
            case Event.PuzzleSolved(pid, _, _, _) =>
              pid -> Agg._Points.modify(_ + 50)
            case Event.GemCollected(pid, gemType, _) =>
              pid -> Agg._Points.modify(_ + 10).andThen {
                Agg._Gems.modify(_.updatedWith(gemType)(_.map(_ + 1).orElse(Some(1))))
              }
          }
          val agg = aggs.getOrElse(playerId, Agg.empty)
          val out = aggs.updated(playerId, modifier(agg))
          val nst = if (tick === Tick.On) Map.empty[PlayerId, Agg] else out

          ticker.merge(tick, count).map { case (newTick, newCount) =>
            (nst -> newCount) -> (out -> newTick)
          }
        case ((aggs, _), (None, _)) =>
          ((Map.empty[PlayerId, Agg] -> 0) -> (aggs -> Tick.On.asInstanceOf[Tick])).pure[F]
      }
  }

}
