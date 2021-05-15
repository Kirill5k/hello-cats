package io.kirill.hellocats.usageexamples

import cats.Monad
import cats.effect.{Clock, Ref, Temporal}
import cats.implicits._

import java.time.Instant
import scala.concurrent.duration.FiniteDuration

trait CacheF[F[_], K, V] {
  def get(key: K): F[Option[V]]
  def exists(key: K): F[Boolean]
  def put(key: K, value: V): F[Unit]
}

private class RefCache[F[_]: Clock: Monad, K, V](
    private val state: Ref[F, Map[K, (Long, V)]]
) extends CacheF[F, K, V] {

  override def get(key: K): F[Option[V]] =
    state.get.map(_.get(key).map { case (_, v) => v })

  override def exists(key: K): F[Boolean] =
    state.get.map(_.contains(key))

  override def put(key: K, value: V): F[Unit] =
    Clock[F].realTime.flatMap { ts =>
      state.update(_.updated(key, (ts.toNanos, value)))
    }
}

object CacheF {
  def of[F[_]: Clock, K, V](
      expiresIn: FiniteDuration,
      checkOnExpirationsEvery: FiniteDuration
  )(implicit F: Temporal[F]): F[CacheF[F, K, V]] = {
    def runExpiration(state: Ref[F, Map[K, (Long, V)]]): F[Unit] = {
      val process = F.realTime.flatMap { ts =>
        state.get
          .map(_.filter { case (_, (exp, _)) => exp + expiresIn.toNanos > ts.toNanos})
          .flatTap(state.set)
      }

      F.sleep(checkOnExpirationsEvery) >> process >> runExpiration(state)
    }

    Ref
      .of[F, Map[K, (Long, V)]](Map.empty)
      .flatTap(s => F.start(runExpiration(s)).void)
      .map(ref => new RefCache[F, K, V](ref))
  }
}
