package io.kirill.hellocats.concurrency

import java.util.concurrent.Executors

import cats.effect.{ContextShift, ExitCode, IO, IOApp}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Shift extends IOApp {

  val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  val cs: ContextShift[IO] = IO.contextShift(ec)

  def printThread(id: String) = {
    val thread = Thread.currentThread.getName
    println(s"[$thread] $id")
  }

  def shiftLoop(id: String)(i: Int): IO[Unit] =
    for {
      _ <- IO(printThread(id))
      _ <- IO.sleep(200.millis)
      _ <- IO.shift(cs) // <--- now we shift!
      result <- shiftLoop(id)(i + 1)
    } yield result

  val shiftProgram = for {
    _ <- shiftLoop("A")(0).start(cs)
    _ <- shiftLoop("B")(0).start(cs)
  } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] =
    shiftProgram
}
