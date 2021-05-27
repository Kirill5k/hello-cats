package io.kirill.hellocats.concurrency

import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object SinglePool extends IOApp.Simple {

  val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

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

  override val run: IO[Unit] =
    for {
      _ <- loop("A")(0).startOn(ec)
      _ <- loop("B")(0).startOn(ec)
    } yield ()
}
