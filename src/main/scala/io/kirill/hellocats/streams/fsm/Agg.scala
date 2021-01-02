package io.kirill.hellocats.streams.fsm

import io.kirill.hellocats.streams.fsm.game._
import monocle.macros._

import java.time.Instant

final case class Points(value: Int) extends AnyVal {
  def +(points: Int): Points =
    Points(value+points)
}

final case class Summary(
    playerId: PlayerId,
    level: Level,
    points: Points,
    gems: Map[GemType, Int],
    createdAt: Instant
)

final case class Agg(
    level: Level,
    points: Points,
    gems: Map[GemType, Int]
) {
  def summary(pid: PlayerId, ts: Instant): Summary =
    Summary(pid, level, points, gems, ts)

  def update(event: Event): Agg = {
    val upd = event match {
      case Event.LevelUp(_, level, _) =>
        Agg._Points.modify(_ + 100).andThen(Agg._Level.set(level))
      case Event.PuzzleSolved(_, _, _, _) =>
        Agg._Points.modify(_ + 50)
      case Event.GemCollected(_, gemType, _) =>
        Agg._Points.modify(_ + 10).andThen {
          Agg._Gems.modify(_.updatedWith(gemType)(_.map(_ + 1).orElse(Some(1))))
        }
    }
    upd(this)
  }
}

object Agg {
  def empty = Agg(Level(0), Points(0), Map.empty)

  val _Gems   = GenLens[Agg](_.gems)
  val _Level  = GenLens[Agg](_.level)
  val _Points = GenLens[Agg](_.points)
}
