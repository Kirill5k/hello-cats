package io.kirill.hellocats.usageexamples

import java.time.Instant

import cats.Monad
import cats.effect.{Clock, Concurrent, Timer}
import cats.implicits._
import cats.effect.concurrent.Ref

import scala.concurrent.duration.FiniteDuration

sealed trait CacheF[F[_], K, V] {
  def get(key: K): F[Option[V]]
  def exists(key: K): F[Boolean]
  def put(key: K, value: V): F[Unit]
}

private class RefCache[F[_]: Clock: Monad, K, V](
    state: Ref[F, Map[K, (Instant, V)]],
    expiresIn: FiniteDuration
  ) extends CacheF[F, K, V] {

  override def get(key: K): F[Option[V]] =
    state.get.map(_.get(key).map { case (_, v) => v})

  override def exists(key: K): F[Boolean] =
    state.get.map(_.contains(key))

  override def put(key: K, value: V): F[Unit] =
    state.update(_.updated(key, (Instant.now.plusNanos(expiresIn.toNanos), value)))
}

object CacheF {
  def of[F[_]: Clock, K, V](
    expiresIn: FiniteDuration,
    checkOnExpirationsEvery: FiniteDuration
  )(implicit T: Timer[F], C: Concurrent[F]): F[CacheF[F, K, V]] = {
    def runExpiration(state: Ref[F, Map[K, (Instant, V)]]): F[Unit] = {
      val process = state.get.map(_.filter {
        case (_, (exp, _)) => exp.isAfter(Instant.now.minusNanos(expiresIn.toNanos))
      }).flatTap(state.set)
      T.sleep(checkOnExpirationsEvery) >> process >> runExpiration(state)
    }

    Ref.of[F, Map[K, (Instant, V)]](Map.empty)
      .flatTap(s => C.start(runExpiration(s)).void)
      .map(ref => new RefCache[F, K, V](ref, expiresIn))
  }
}