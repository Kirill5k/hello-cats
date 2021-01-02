package io.kirill.hellocats.streams.fsm

import cats.Parallel
import cats.effect._
import cats.implicits._
import fs2.Pipe
import io.kirill.hellocats.streams.fsm.game._

final case class Engine[F[_]: Concurrent: Parallel: Time: Timer](
    publish: Summary => F[Unit],
    ticker: Ticker[F]
) {

  def run: Pipe[F, Event, Unit] =
    _.noneTerminate
      .zip(ticker.ticks)
      .mapAccumulate(Map.empty[PlayerId, Agg])(Engine.fsm.run)
      .collect { case (_, (outputState, Tick.On)) => outputState }
      .evalMap { state =>
        Time[F].now.flatMap { ts =>
          state.toList.parTraverse_ { case (pid, agg) =>
            publish(agg.summary(pid, ts))
          }
        }
      }
}

object Engine {
  type State  = Map[PlayerId, Agg]
  type Input  = (Option[Event], Tick)
  type Output = (State, Tick)

  def fsm: FSM[State, Input, Output] = new FSM[State, Input, Output] {
    override def run(state: State, input: Input): (State, Output) =
      (state, input) match {
        case (aggs, (Some(event), tick)) =>
          val agg       = aggs.getOrElse(event.playerId, Agg.empty)
          val out       = aggs.updated(event.playerId, agg.update(event))
          val nextState = if (tick === Tick.On) Map.empty[PlayerId, Agg] else out

          nextState -> (out -> tick)
        case (aggs, (None, _)) =>
          Map.empty[PlayerId, Agg] -> (aggs -> Tick.On.asInstanceOf[Tick])
      }
  }

}
