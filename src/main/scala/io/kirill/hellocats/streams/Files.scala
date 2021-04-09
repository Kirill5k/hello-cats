package io.kirill.hellocats.streams

import cats.effect.{IO, IOApp}
import cats.implicits._

import java.nio.file.{Paths, StandardOpenOption}
import fs2.{io, text}

object Files extends IOApp.Simple {

  override def run: IO[Unit] = {
    val filesOps = List(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
    val input    = getClass().getClassLoader().getResource("2021-01-15.txt")
    val output   = getClass().getClassLoader().getResource("logs.txt")
    IO.println(input.toString) *>
      IO.println(output.toString) *>
      io.file
        .Files[IO]
        .readAll(Paths.get(input.toURI), 4096)
        .through(text.utf8Decode)
        .through(text.lines)
        .filter(_.contains("INFO"))
        .map(_ + "\n")
        .through(text.utf8Encode)
        .through(io.file.Files[IO].writeAll(Paths.get("logs.txt"), filesOps))
        .compile
        .drain
  }
}
