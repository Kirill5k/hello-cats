package io.kirill.hellocats.usageexamples

import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.Monad
import cats.effect.{Clock, Concurrent, Timer}
import cats.implicits._
import cats.effect.concurrent.Ref

import scala.concurrent.duration.FiniteDuration

sealed trait Cache[F[_], K, V] {
  def get(key: K): F[Option[V]]
  def exists(key: K): F[Boolean]
  def put(key: K, value: V): F[Unit]
}

private class RefCache[F[_]: Clock: Concurrent, K, V](
    expiresIn: FiniteDuration,
    checkOnExpirationsEvery: FiniteDuration
  )(implicit T: Timer[F]) extends Cache[F, K, V] {

  private def runExpiration(currentState: Ref[F, Map[K, (Instant, V)]]): F[Unit] = {
    val process = currentState.get.map(_.filter {
      case (_, (exp, _)) => exp.isAfter(Instant.now.minusNanos(expiresIn.toNanos))
    }).flatTap(currentState.set)
    T.sleep(checkOnExpirationsEvery) >> process >> runExpiration(currentState)
  }

  private val state = Ref.of[F, Map[K, (Instant, V)]](Map.empty).flatTap(runExpiration(_))

  override def get(key: K): F[Option[V]] =
    state.flatMap(_.get.map(_.get(key).map { case (_, v) => v}))

  override def exists(key: K): F[Boolean] =
    state.flatMap(_.get.map(_.contains(key)))

  override def put(key: K, value: V): F[Unit] =
    state.flatMap(_.update(_.updated(key, (Instant.now.plusNanos(expiresIn.toNanos), value))))
}

object Cache {
  def refCache[F[_]: Clock: Concurrent, K, V](
    expiresIn: FiniteDuration,
    checkOnExpirationsEvery: FiniteDuration
  )(implicit T: Timer[F]): Cache[F, K, V] = new RefCache(expiresIn, checkOnExpirationsEvery)
}