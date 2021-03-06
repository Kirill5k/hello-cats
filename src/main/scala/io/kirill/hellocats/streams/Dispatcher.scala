package io.kirill.hellocats.streams

import cats.effect.kernel.Async
import cats.effect.{ExitCode, IO, IOApp, Temporal}
import cats.implicits._
import fs2.Stream
import io.kirill.hellocats.utils.printing._

import scala.concurrent.duration._

object Dispatcher extends IOApp {

  /**
   * Params:
   * - number-of-devices
   * Requirements:
   * - each device periodically (every minute) sends a message.
   * - mps (number-of-devices / 60)
   */

  def sendRequests[F[_]: Async](payloads: List[String]): F[Unit] =
    appendLog("\n") *> log(s"sending ${payloads.size} requests") *>
      appendLog("- ") *>
      payloads.traverse_(p => appendLog(s"req $p ") *> Temporal[F].sleep(100.millis))

  def deviceFeed[F[_]: Async](numberOfDevices: Int): Stream[F, Unit] =
    Stream
      .range(0, numberOfDevices)
      .chunkN(numberOfDevices / 60)
      .evalMap(c => sendRequests(c.map(_.toString).toList))
      .metered(1.second)
      .repeat

  def deviceFeed2[F[_]: Async](numberOfDevices: Int): Stream[F, Unit] = {
    val delay = if (numberOfDevices < 60) 60.0 / numberOfDevices * 1000 else 1000
    val groupsize = if (numberOfDevices < 60) 1 else numberOfDevices / 60
    Stream
      .range(0, numberOfDevices)
      .chunkN(groupsize)
      .evalMap(c => sendRequests(c.map(_.toString).toList))
      .zipLeft(Stream.awakeEvery[F](delay.millis))
      .repeat
  }


  override def run(args: List[String]): IO[ExitCode] =
    deviceFeed2[IO](600).compile.drain.as(ExitCode.Success)
}
