package io.kirill.hellocats.usageexamples

import cats.effect.concurrent.Deferred
import cats.effect.{Concurrent, Timer}
import cats.implicits._

import scala.concurrent.duration.FiniteDuration

object HappyEyeballs {

  def run[F[_], A](tasks: List[F[A]], delay: FiniteDuration)(implicit T: Timer[F], C: Concurrent[F]): F[A] =
    tasks match {
      case Nil         => Concurrent[F].raiseError(new IllegalArgumentException("empty tasks list provided"))
      case task :: Nil => task
      case task :: otherTasks =>
        Deferred[F, Unit].flatMap { errorTrigger =>
          val taskWithErrorSignal  = task.handleErrorWith(_ => errorTrigger.complete(()) *> C.never[A])
          val waitForDelayOrSignal = C.race(T.sleep(delay), errorTrigger.get)
          C.race(taskWithErrorSignal, waitForDelayOrSignal *> run(otherTasks, delay)).map(_.fold(l => l, r => r))
        }
    }
}
