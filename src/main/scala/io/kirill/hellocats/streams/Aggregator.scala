package io.kirill.hellocats.streams

import cats.effect.kernel.Async
import cats.effect.std.Queue
import cats.effect._
import cats.implicits._
import fs2.Stream
import io.kirill.hellocats.utils.printing._

import scala.concurrent.duration._
import scala.util.Random

object Aggregator extends IOApp.Simple {

  val rand = Random

  final case class Query(
      firstname: String,
      lastname: String
  )

  final case class Quote(
      providerName: String,
      price: BigDecimal
  )

  val providers: List[String] = List("p1", "p2", "p3", "p4")

  def queryProvider[F[_]: Async](providerName: String, query: Query): F[Option[Quote]] =
    log[F](s"querying provider $providerName - $query") *>
      Temporal[F].sleep((rand.nextDouble() * 10000).millis) *>
      Sync[F].delay(Some(Quote(providerName, BigDecimal(rand.nextDouble() * 100))))

  def queryProviders[F[_]: Async](query: Query): Stream[F, Quote] =
    Stream
      .emits(providers)
      .map(p => Stream.eval(queryProvider[F](p, query)))
      .parJoinUnbounded
      .unNone

  override def run: IO[Unit] =
    for {
      quotes <- Queue.unbounded[IO, Option[Quote]]
      _ <- Stream
        .bracket(IO.pure(quotes))(_.offer(None))
        .flatMap(q => queryProviders[IO](Query("Foo", "Bar")).evalMap(quote => q.offer(Some(quote))))
        .compile
        .drain
        .start
      _ <- Stream.fromQueueNoneTerminated(quotes)
        .evalTap(quote => log[IO](s"received quote from provider ${quote.providerName}"))
        .compile
        .drain
    } yield ()
}
