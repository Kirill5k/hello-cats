package io.kirill.hellocats.usageexamples

import cats.effect.Temporal
import cats.implicits._

import scala.concurrent.duration.FiniteDuration

trait Background[F[_]] {
  def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit]
}

object Background {
  implicit def concurrentBackground[F[_]: Temporal]: Background[F] =
    new Background[F] {
      override def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit] = {
        Temporal[F].start(Temporal[F].sleep(duration) *> fa).void
      }
    }

  def apply[F[_]](implicit ev: Background[F]): Background[F] = ev


  implicit class BackgroundOps[F[_], A](fa: F[A])(implicit B: Background[F]) {
    def schedule(duration: FiniteDuration): F[Unit] =
      B.schedule(fa, duration)
  }
}
