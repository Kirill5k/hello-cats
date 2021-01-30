package io.kirill.hellocats.effects

import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._

/**
 * unbounded queue, `dequeue` semantically blocks on empty queue
  */
trait Queue[F[_], A] {
  def enqueue(a: A): F[Unit]
  def dequeue: F[A]
}



object Queue {

  def unbounded[F[_]: Concurrent, A]: F[Queue[F, A]] = ???
}
