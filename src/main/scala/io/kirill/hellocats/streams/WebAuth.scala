package io.kirill.hellocats.streams

import java.util.UUID
import cats.effect.{Async, IO, IOApp, Sync, Temporal}
import cats.implicits._
import fs2.Stream
import fs2.concurrent.SignallingRef
import io.kirill.hellocats.utils.printing._

import scala.concurrent.duration._

object WebAuth extends IOApp.Simple {

  final case class AuthToken(token: String, expiresIn: Long)

  final case class Item(id: String)
  final case class ItemsResponse(
      items: List[Item],
      page: Int,
      nextPage: Option[Int]
  )

  def authenticate[F[_]: Async]: F[AuthToken] = {
    log[F]("authenticating") *>
      Temporal[F].sleep(200.millis) *>
      Sync[F].delay(AuthToken(UUID.randomUUID().toString, 5000))
  }

  def authStream[F[_]: Async](authToken: SignallingRef[F, AuthToken]): Stream[F, Unit] =
    Stream
      .eval(authToken.get)
      .flatMap { t =>
        Stream
          .repeatEval(authenticate[F].flatMap(authToken.set))
          .metered(t.expiresIn.millis)
      }

  def getItem[F[_]: Async](token: String, page: Int): F[ItemsResponse] =
    log(s"getting item from page $page with token $token") *>
      Temporal[F].sleep(1000.millis) *>
      Sync[F].delay(
        ItemsResponse(
          List(Item(UUID.randomUUID().toString)),
          page,
          if (page < 25) Some(page + 1) else None
        )
      )

  def itemStream[F[_]: Async](authToken: SignallingRef[F, AuthToken]): Stream[F, Item] =
    Stream
      .unfoldLoopEval[F, Int, List[Item]](0) { page =>
        authToken.get.flatMap(t => getItem(t.token, page).map(r => (r.items, r.nextPage)))
      }
      .flatMap(Stream.emits)

  override val run: IO[Unit] =
    Stream
      .eval(authenticate[IO].flatMap(SignallingRef[IO, AuthToken]))
      .flatMap { token =>
        authStream[IO](token).mergeHaltBoth(itemStream[IO](token))
      }
      .compile
      .drain
}
