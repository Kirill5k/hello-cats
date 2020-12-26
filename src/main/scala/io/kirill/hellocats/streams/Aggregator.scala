package io.kirill.hellocats.streams

import cats.effect.{Concurrent, ExitCode, IO, IOApp, Sync, Timer}
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Queue
import utils._

import scala.util.Random
import scala.concurrent.duration._

object Aggregator extends IOApp {

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

  def queryProvider[F[_]: Sync: Timer](providerName: String, query: Query): F[Option[Quote]] =
    log[F](s"querying provider $providerName - $query") *>
      Timer[F].sleep((rand.nextDouble() * 10000).millis) *>
      Sync[F].delay(Some(Quote(providerName, BigDecimal(rand.nextDouble() * 100))))

  def queryProviders[F[_]: Concurrent: Timer](query: Query): Stream[F, Quote] =
    Stream
      .emits(providers)
      .map(p => Stream.eval[F, Option[Quote]](queryProvider[F](p, query)))
      .parJoinUnbounded[F, Option[Quote]]
      .unNone

  override def run(args: List[String]): IO[ExitCode] =
    for {
      quotes <- Queue.noneTerminated[IO, Quote]
      _ <- Stream
        .bracket(IO.pure(quotes))(_.enqueue1(None))
        .flatMap(q => queryProviders[IO](Query("Foo", "Bar")).evalMap(quote => q.enqueue1(Some(quote))))
        .compile
        .drain
        .start
      _ <- quotes.dequeue
        .evalTap(quote => log[IO](s"received quote from provider ${quote.providerName}"))
        .compile
        .drain
    } yield ExitCode.Success
}
