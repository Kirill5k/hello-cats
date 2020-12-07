package io.kirill.hellocats.usageexamples.happyeyeballs

import cats.effect.{Concurrent, Timer}
import cats.implicits._

import scala.concurrent.duration.FiniteDuration

object HappyEyeballs {

  def basic[F[_]: Concurrent: Timer](effects: List[F[Unit]], delay: FiniteDuration): F[Unit] =
    effects match {
      case Nil =>
        Concurrent[F].raiseError(new IllegalArgumentException(""))
      case head :: Nil =>
        head
      case head :: tail =>
        Concurrent[F].race(head, Timer[F].sleep(delay) *> basic(tail, delay)).map { _ =>
          ()
        }

    }
}
