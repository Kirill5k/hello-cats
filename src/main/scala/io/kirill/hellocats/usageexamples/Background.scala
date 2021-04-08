package io.kirill.hellocats.usageexamples

import cats.effect.Temporal
import cats.effect.kernel.{Concurrent, Resource}
import cats.effect.std.Queue
import cats.implicits._
import fs2.Stream

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

  def resource[F[_]: Temporal]: Resource[F, Background[F]] =
    Stream
      .eval(Queue.unbounded[F, (FiniteDuration, F[Any])])
      .flatMap { q =>
        val bg = new Background[F] {
          override def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit] =
            q.offer(duration -> fa.widen)
        }
        val process = Stream
          .fromQueueUnterminated(q)
          .map { case (duration, fa) => Stream.attemptEval(fa).delayBy(duration) }
          .parJoinUnbounded
        Stream.emit(bg).concurrently(process)
      }
      .compile
      .resource
      .lastOrError

}
