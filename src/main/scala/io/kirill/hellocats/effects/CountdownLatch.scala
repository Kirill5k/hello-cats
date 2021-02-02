package io.kirill.hellocats.effects

import cats.Monad
import cats.effect.Concurrent
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._

trait CountdownLatch[F[_]] {
  def await(): F[Unit]
  def decrement(): F[Unit]
}

final private class RefbasedCountdownLatch[F[_]](
    private val counts: Ref[F, Int],
    private val latch: Deferred[F, Unit]
)(implicit
    val F: Monad[F]
) extends CountdownLatch[F] {

  override def await(): F[Unit] =
    latch.get

  override def decrement(): F[Unit] =
    counts.updateAndGet(_ - 1).flatMap { currentCount =>
      if (currentCount == 0) latch.complete(()) else F.unit
    }
}

object CountdownLatch {
  def make[F[_]: Concurrent](count: Int): F[CountdownLatch[F]] =
    for {
      count <- Ref.of[F, Int](count)
      latch <- Deferred[F, Unit]
    } yield new RefbasedCountdownLatch[F](count, latch)
}