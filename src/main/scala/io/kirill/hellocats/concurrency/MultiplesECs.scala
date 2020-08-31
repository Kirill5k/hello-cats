package io.kirill.hellocats.concurrency

import java.util.concurrent.Executors

import cats.effect.{ContextShift, ExitCode, IO, IOApp}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object MultiplesECs extends IOApp {

  val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  val ec2 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  val cs1: ContextShift[IO] = IO.contextShift(ec1)
  val cs2: ContextShift[IO] = IO.contextShift(ec2)

  def loop(id: String)(i: Int): IO[Unit] = for {
    _ <- IO(println(id, i))
    _ <- IO.sleep(200.millis)
    _ <- if (i == 10) IO.shift(cs1) else IO.unit
    result <- loop(id)(i + 1)
  } yield result

  val program = for {
    _ <- loop("A")(0).start(cs1)
    _ <- loop("B")(0).start(cs2)
  } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] = program
}
