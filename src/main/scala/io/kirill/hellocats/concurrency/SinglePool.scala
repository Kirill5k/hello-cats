package io.kirill.hellocats.concurrency

import java.util.concurrent.Executors

import cats.effect.{ContextShift, ExitCode, IO, IOApp}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object SinglePool extends IOApp {

  val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  val cs: ContextShift[IO]         = IO.contextShift(ec)

  def printThread(id: String): Unit = {
    val thread = Thread.currentThread.getName
    println(s"[$thread] $id")
  }

  def loop(id: String)(i: Int): IO[Unit] =
    for {
      _      <- IO(printThread(id))
      _      <- IO(Thread.sleep(200))
      result <- loop(id)(i + 1)
    } yield result

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- loop("A")(0).start(cs)
      _ <- loop("B")(0).start(cs)
    } yield ExitCode.Success
}
