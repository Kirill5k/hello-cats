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
}

object Agg {
  def empty = Agg(Level(0), Points(0), Map.empty)

  val _Gems   = GenLens[Agg](_.gems)
  val _Level  = GenLens[Agg](_.level)
  val _Points = GenLens[Agg](_.points)
}
