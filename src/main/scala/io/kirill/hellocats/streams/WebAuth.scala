package io.kirill.hellocats.streams

import java.util.UUID
import cats.effect.{ExitCode, IO, IOApp, Sync, Timer}
import cats.implicits._
import fs2.Stream
import fs2.concurrent.SignallingRef
import utils._

import scala.concurrent.duration._

object WebAuth extends IOApp {

  final case class AuthToken(token: String, expiresIn: Long)

  final case class Item(id: String)
  final case class ItemsResponse(
      items: List[Item],
      page: Int,
      nextPage: Option[Int]
  )

  def authenticate[F[_]: Sync: Timer]: F[AuthToken] =
    log("authenticating") *>
      Timer[F].sleep(200.millis) *>
      Sync[F].delay(AuthToken(UUID.randomUUID().toString, 5000))

  def authStream[F[_]: Sync: Timer]: Stream[F, String] =
    Stream
      .eval(authenticate)
      .flatMap(t => Stream(t.token).covary[F] ++ Stream.sleep_[F](t.expiresIn.millis))
      .repeat

  def getItem[F[_]: Sync: Timer](token: String, page: Int): F[ItemsResponse] =
    log(s"getting item from page $page with token $token") *>
      Timer[F].sleep(1000.millis) *>
      Sync[F].delay(
        ItemsResponse(
          List(Item(UUID.randomUUID().toString)),
          page,
          if (page < 10) Some(page + 1) else None
        )
      )

  def itemStream[F[_]: Sync: Timer](authToken: SignallingRef[F, String]): Stream[F, Item] =
    Stream
      .unfoldEval[F, Option[Int], List[Item]](Some(0)) { currentPage =>
        currentPage.traverse { page =>
          authToken.get.flatMap(t => getItem(t, page).map(r => (r.items, r.nextPage)))
        }
      }
      .flatMap(Stream.emits)
      .repeat

  override def run(args: List[String]): IO[ExitCode] =
    Stream
      .eval(authenticate[IO].flatMap(t => SignallingRef[IO, String](t.token)))
      .flatMap { token =>
        val tokens = authStream[IO].evalMap(newToken => token.set(newToken)).drain
        val items  = itemStream[IO](token)
        tokens.merge(items)
      }
      .compile
      .drain
      .as(ExitCode.Success)
}
