package io.kirill.hellocats.usageexamples

import cats.effect.{Concurrent, Temporal}
import cats.effect.kernel.Deferred
import cats.implicits._

import scala.concurrent.duration.FiniteDuration

object HappyEyeballs {

  def run[F[_], A](tasks: List[F[A]], delay: FiniteDuration)(implicit F: Temporal[F]): F[A] =
    tasks match {
      case Nil         => Concurrent[F].raiseError(new IllegalArgumentException("empty tasks list provided"))
      case task :: Nil => task
      case task :: otherTasks =>
        Deferred[F, Unit].flatMap { errorTrigger =>
          val taskWithErrorSignal  = task.handleErrorWith(_ => errorTrigger.complete(()) *> F.never[A])
          val waitForDelayOrSignal = F.race(F.sleep(delay), errorTrigger.get)
          F.race(taskWithErrorSignal, waitForDelayOrSignal *> run(otherTasks, delay)).map(_.fold(l => l, r => r))
        }
    }
}
