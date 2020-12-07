package io.kirill.hellocats.usageexamples.happyeyeballs

import cats.effect.concurrent.Deferred
import cats.effect.{Concurrent, Timer}
import cats.implicits._

import scala.concurrent.duration.FiniteDuration

object HappyEyeballs {

  def run[F[_]: Concurrent: Timer, A](tasks: List[F[A]], delay: FiniteDuration): F[A] =
    tasks match {
      case Nil =>
        Concurrent[F].raiseError(new IllegalArgumentException(""))
      case head :: Nil =>
        head
      case head :: tail =>
        Deferred[F, Unit].flatMap { errorTrigger =>
          val task1 = head.handleErrorWith(e => errorTrigger.complete(()) *> Concurrent[F].raiseError(e))
          val waitForDelayOrError = Concurrent[F].race(Timer[F].sleep(delay), errorTrigger.get)
          Concurrent[F].race(task1, waitForDelayOrError *> run(tail, delay)).map {
            case Left(res) => res
            case Right(res) => res
          }
        }
    }
}
