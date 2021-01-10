package io.kirill.hellocats.streams

import cats.implicits._
import cats.effect._
import concurrent._

/** unbounded queue, `dequeue` semantically blocks on empty queue
  */
trait Queue[F[_], A] {
  def enqueue(a: A): F[Unit]
  def dequeue: F[A]
}

final private class UnboundedQueue[F[_]: Concurrent, A](
    private val state: Ref[F, (Vector[A], Vector[Deferred[F, A]])]
) extends Queue[F, A] {

  override def enqueue(a: A): F[Unit] =
    state.modify {
      case (elems, deqs) if deqs.isEmpty =>
        (elems :+ a, deqs) -> ().pure[F]
      case (elems, deq +: deqs) =>
        (elems, deqs) -> deq.complete(a)
    }.flatten

  override def dequeue: F[A] =
    Deferred[F, A].flatMap { wait =>
      state.modify {
        case (elems, deqs) if elems.isEmpty =>
          (elems, deqs :+ wait) -> wait.get
        case (e +: elems, deqs) =>
          (elems, deqs) -> e.pure[F]
      }.flatten
    }
}

object Queue {
  def unbounded[F[_]: Concurrent, A]: F[Queue[F, A]] =
    Ref[F]
      .of(Vector.empty[A] -> Vector.empty[Deferred[F, A]])
      .map(state => new UnboundedQueue[F, A](state))
}
