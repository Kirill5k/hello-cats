package io.kirill.hellocats.streams.fsm

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration.FiniteDuration

object game {

  final case class PlayerId(value: UUID)     extends AnyVal
  final case class Level(value: Int)         extends AnyVal
  final case class PlayerScore(value: Int)   extends AnyVal
  final case class PuzzleName(value: String) extends AnyVal

  final case class Game(
      playerId: PlayerId,
      playerScore: PlayerScore,
      level: Level,
      gems: Map[GemType, Int]
  )

  final case class Player(
      id: PlayerId,
      highestScore: PlayerScore,
      gems: Map[GemType, Int],
      memberSince: Instant
  )

  sealed trait GemType
  object GemType {
    final case object Diamond  extends GemType
    final case object Emerald  extends GemType
    final case object Ruby     extends GemType
    final case object Sapphire extends GemType
  }

  sealed trait Event {
    def playerId: PlayerId
    def createdAt: Instant
  }

  object Event {
    final case class LevelUp( // 100 points
        playerId: PlayerId,
        newLevel: Level,
        createdAt: Instant
    ) extends Event

    final case class PuzzleSolved( // 50 points
        playerId: PlayerId,
        puzzleName: PuzzleName,
        duration: FiniteDuration,
        createdAt: Instant
    ) extends Event

    final case class GemCollected( // 10 points
        playerId: PlayerId,
        gemType: GemType,
        createdAt: Instant
    ) extends Event
  }
}
