package io.kirill.hellocats.usageexamples

import cats.effect.{Concurrent, Timer}
import cats.implicits._
import io.kirill.hellocats.typeclasses.Number
import org.log4s.Logger

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

  def apply[F[_]](implicit ev: Background[F]): Background[F] = ev


  implicit class BackgroundOps[F[_], A](fa: F[A])(implicit B: Background[F]) {
    def schedule(duration: FiniteDuration): F[Unit] =
      B.schedule(fa, duration)
  }
}
