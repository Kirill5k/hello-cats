package io.kirill.hellocats.streams

import java.time.LocalTime

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.implicits._
import fs2.Stream

import scala.concurrent.duration._

object Metered extends IOApp {

  val timeStream = Stream
    .repeatEval(IO(println(LocalTime.now)))
    .evalTap(_ => IO(println("foo")))
    .metered(1.second)

  val immediateTimeStream = Stream
    .repeatEval(IO(println(LocalTime.now)))
    .evalTap(_ => IO.sleep(1.second))

  val anotherImmediateStream = Stream
    .repeatEval(IO(println(LocalTime.now)))
    .zipLeft(Stream.awakeEvery[IO](1.second))

  override def run(args: List[String]): IO[ExitCode] =
   IO(println(s"${LocalTime.now()} starting stream")) *> anotherImmediateStream.compile.drain.as(ExitCode.Success)

}