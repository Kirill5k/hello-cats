package io.kirill.hellocats.streams

import java.time.LocalTime
import cats.effect.{ExitCode, IO, IOApp, Sync, Timer}
import cats.implicits._
import fs2.Stream

import scala.concurrent.duration._

object Dispatcher extends IOApp{

  /**
   * Params:
   * - number-of-devices
   * - mps (number-of-devices / 60)
   * Requirements:
   * - each device periodically sends a message every minute.
   */


  def sendRequest[F[_]: Sync](payload: String): F[Unit] =
    Sync[F].delay(println(s"${LocalTime.now()}: sending req ${payload}"))

  def requestFeed[F[_]: Sync: Timer](numberOfDevices: Int): Stream[F, Unit] =
    Stream
      .range(0, numberOfDevices)
      .chunkN(numberOfDevices / 60)
      .evalMap(c => c.traverse(i => sendRequest(i.toString)).void)
      .metered(1.second)
      .repeat


  override def run(args: List[String]): IO[ExitCode] =
    requestFeed[IO](60).compile.drain.as(ExitCode.Success)
}
