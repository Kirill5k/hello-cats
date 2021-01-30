package io.kirill.hellocats.effects

import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._

/** unbounded queue, `dequeue` semantically blocks on empty queue
  */
trait Queue[F[_], A] {
  def enqueue(e: A): F[Unit]
  def dequeue: F[A]
}

final private class UnboundedQueue[F[_]: Concurrent, A](
    private val state: Ref[F, (Vector[A], Vector[Deferred[F, A]])]
) extends Queue[F, A] {

  override def enqueue(e: A): F[Unit] =
    state.get.flatMap {
      case (els, waits) if waits.isEmpty =>
        state.set((els :+ e, waits))
      case (els, w +: waits) =>
        state.set((els, waits)) *> w.complete(e)
    }

  override def dequeue: F[A] =
    state.get.flatMap {
      case (els, waits) if els.isEmpty =>
        Deferred[F, A].flatMap { w =>
          state.set((els, w +: waits)) *> w.get
        }
      case (e +: els, waits) =>
        state.set((els, waits)) *> e.pure[F]
    }
}

object Queue {

  def unbounded[F[_]: Concurrent, A]: F[Queue[F, A]] =
    Ref
      .of((Vector.empty[A], Vector.empty[Deferred[F, A]]))
      .map(state => new UnboundedQueue[F, A](state))
}
