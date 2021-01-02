package io.kirill.hellocats.streams.fsm

import io.kirill.hellocats.streams.fsm.game._

import java.time.Instant

final case class Points(value: Int) extends AnyVal {
  def +(points: Int): Points = Points(value + points)
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

  def update(event: Event): Agg =
    event match {
      case Event.LevelUp(_, lvl, _) =>
        copy(level = lvl, points = points + 100)
      case Event.PuzzleSolved(_, _, _, _) =>
        copy(points = points + 50)
      case Event.GemCollected(_, gemType, _) =>
        copy(points = points + 10, gems = gems.updatedWith(gemType)(_.map(_ + 1).orElse(Some(1))))
    }
}

object Agg {
  def empty = Agg(Level(0), Points(0), Map.empty)
}
