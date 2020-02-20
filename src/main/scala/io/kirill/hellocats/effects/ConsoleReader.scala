package io.kirill.hellocats.effects

import cats._
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}

trait Console[F[_]] {
  def print(text: String): F[Unit]
  def read: F[String]
}

object Console {
  implicit val stdConsole = new Console[IO] {
    override def print(text: String): IO[Unit] = IO(println(text))
    override def read: IO[String] = IO(scala.io.StdIn.readLine)
  }
}

object ConsoleReader extends IOApp {

  import Console._

  def program[F[_]: Monad](implicit C: Console[F]): F[Unit] =
    for {
      _ <- C.print("enter your name")
      name <- C.read
      _ <- C.print(s"Hello, $name!")
    } yield ()

  override def run(args: List[String]): IO[ExitCode] = program[IO].as(ExitCode.Success)
}
