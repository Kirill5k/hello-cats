package io.kirill.hellocats.concurrency

import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object MultiplesECs extends IOApp {

  val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  val ec2 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  def loop(id: String)(i: Int): IO[Unit] = for {
    _ <- IO(println(id, i))
    _ <- IO.sleep(200.millis)
    result <- loop(id)(i + 1)
  } yield result

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- loop("A")(0).startOn(ec1)
      _ <- loop("B")(0).startOn(ec2)
    } yield ExitCode.Success
}
