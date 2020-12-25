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

  def authStream[F[_]: Sync: Timer](authToken: SignallingRef[F, AuthToken]): Stream[F, Unit] =
    Stream
      .eval(authToken.get)
      .flatMap { t =>
        Stream
          .repeatEval(authenticate.flatMap(authToken.set))
          .metered(t.expiresIn.millis)
      }

  def getItem[F[_]: Sync: Timer](token: String, page: Int): F[ItemsResponse] =
    log(s"getting item from page $page with token $token") *>
      Timer[F].sleep(1000.millis) *>
      Sync[F].delay(
        ItemsResponse(
          List(Item(UUID.randomUUID().toString)),
          page,
          if (page < 25) Some(page + 1) else None
        )
      )

  def itemStream[F[_]: Sync: Timer](authToken: SignallingRef[F, AuthToken]): Stream[F, Item] =
    Stream
      .unfoldLoopEval[F, Int, List[Item]](0) { page =>
        authToken.get.flatMap(t => getItem(t.token, page).map(r => (r.items, r.nextPage)))
      }
      .flatMap(Stream.emits)

  override def run(args: List[String]): IO[ExitCode] =
    Stream
      .eval(authenticate[IO].flatMap(SignallingRef[IO, AuthToken]))
      .flatMap { token =>
        authStream[IO](token).mergeHaltBoth(itemStream[IO](token))
      }
      .compile
      .drain
      .as(ExitCode.Success)
}
