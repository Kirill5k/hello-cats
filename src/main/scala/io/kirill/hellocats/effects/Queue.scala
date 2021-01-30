package io.kirill.hellocats.effects

import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._

/** unbounded queue, `dequeue` semantically blocks on empty queue
  */
trait Queue[F[_], A] {
  def enqueue(a: A): F[Unit]
  def dequeue: F[A]
}

final private class UnboundedQueue[F[_]: Concurrent, A](
    private val state: Ref[F, (Vector[A], Vector[Deferred[F, A]])]
) extends Queue[F, A] {

  override def enqueue(a: A): F[Unit] = ???

  override def dequeue: F[A] = ???
}

object Queue {

  def unbounded[F[_]: Concurrent, A]: F[Queue[F, A]] =
    Ref
      .of((Vector.empty[A], Vector.empty[Deferred[F, A]]))
      .map(state => new UnboundedQueue[F, A](state))
}
