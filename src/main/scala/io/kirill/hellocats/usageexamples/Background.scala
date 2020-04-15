package io.kirill.hellocats.usageexamples

import cats.effect.{Concurrent, Timer}
import cats.implicits._

import scala.concurrent.duration.FiniteDuration

trait Background[F[_]] {
  def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit]
}

object Background {
  implicit def concurrentBackground[F[_]: Concurrent: Timer] =
    new Background[F] {
      override def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit] =
        Concurrent[F].start(Timer[F].sleep(duration) *> fa).void
    }
}
