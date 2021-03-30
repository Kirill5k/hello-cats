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

  override def enqueue(e: A): F[Unit] = {
    state.modify {
      case (els, waits) if waits.isEmpty =>
        ((els :+ e, waits), ().pure[F])
      case (els, w +: waits) =>
        ((els, waits), w.complete(e).void)
    }.flatten
  }

  override def dequeue: F[A] =
    Deferred[F, A].flatMap { w =>
      state.modify {
        case (els, waits) if els.isEmpty =>
          ((els, w +: waits), w.get)
        case (e +: els, waits) =>
          ((els, waits), e.pure[F])
      }.flatten
    }

}

object Queue {

  def unbounded[F[_]: Concurrent, A]: F[Queue[F, A]] =
    Ref
      .of((Vector.empty[A], Vector.empty[Deferred[F, A]]))
      .map(state => new UnboundedQueue[F, A](state))
}
