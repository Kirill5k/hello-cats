package io.kirill.hellocats.effects

import cats.effect.implicits._
import cats.effect.kernel.Deferred
import cats.effect.{Concurrent, Fiber, Ref, Temporal}
import cats.implicits._

import scala.concurrent.duration._

/**
 * Resettable timeout
 */
trait Alarm[F[_]] {
  /**
   * Sets the alarm `d` into the future
   * Cancels the last reset
   */
  def reset(d: FiniteDuration): F[Unit]

  /**
   *  Completes `d` after `reset`. Never completes if called before `reset`
   *  The purpose of `buzz` is to be raced with something else.
   */
  def buzz: F[Unit]

  /**
   * Cancels the last reset. `buzz` will never complete.
   */
  def cancel: F[Unit]
}
object Alarm {
  def create[F[_]](implicit F: Temporal[F]): F[Alarm[F]] =
    for {
      empty <- Deferred[F, Fiber[F, Throwable, Unit]]
      state <- Ref[F].of(empty)
      fb <- F.never[Unit].start
      _ <- empty.complete(fb)
    } yield new Alarm[F] {

      def reset(d: FiniteDuration): F[Unit] = for {
        newFiber <- Deferred[F, Fiber[F, Throwable, Unit]]
        oldFiber <- state.getAndSet(newFiber)
        _ <- oldFiber.get.flatMap(_.cancel)
        fiber <- F.sleep(d).start
        _ <- newFiber.complete(fiber)
      } yield ()

      def buzz: F[Unit] = for {
        d <- state.get
        fb <- d.get
        _ <- fb.join
      } yield ()

      def cancel: F[Unit] = for {
        d <- state.get
        fb <- d.get
        _ <- fb.cancel
      } yield ()
    }
}
