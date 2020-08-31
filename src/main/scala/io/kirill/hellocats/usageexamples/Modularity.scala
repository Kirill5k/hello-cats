package io.kirill.hellocats.usageexamples

import cats.{Monad, Parallel}
import cats.effect.{Concurrent, Resource}
import cats.implicits._

trait Users[F[_]]
trait Items[F[_]]
trait EventsManager[F[_]]
trait EventsPublisher[F[_]]
trait KafkaClient[F[_]]
trait HttpClient[F[_]]
trait Cache[F[_]]

object KafkaClient {
  def make[F[_]]: Resource[F, KafkaClient[F]] = ???
}

object HttpClient {
  def make[F[_]]: Resource[F, HttpClient[F]] = ???
}

trait Algebras[F[_]] {
  def users: Users[F]
  def items: Items[F]
}

trait Events[F[_]] {
  def manager: EventsManager[F]
  def publisher: EventsPublisher[F]
}

trait Database[F[_]] {
  def cache: Cache[F]
}

trait Clients[F[_]] {
  def kafka: KafkaClient[F]
  def http: HttpClient[F]
}

object Client {
  def make[F[_]: Concurrent]: Resource[F, Clients[F]] =
    (KafkaClient.make[F], HttpClient.make[F]).mapN {
      case (k, h) =>
        new Clients[F] {
          override def kafka: KafkaClient[F] = k
          override def http: HttpClient[F]   = h
        }
    }
}

object MainProgram {
  def program[F[_]: Monad: Parallel](
      algebras: Algebras[F],
      events: Events[F],
      cache: Cache[F],
      clients: Clients[F]
  ): F[Unit] = ???
}
