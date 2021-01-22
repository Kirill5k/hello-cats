package io.kirill.hellocats.streams

import cats.effect.{Blocker, ExitCode, IO, IOApp}
import io.kirill.hellocats.utils.printing._
import fs2._

import java.nio.file.{Paths, StandardOpenOption}


object Files extends IOApp {


  override def run(args: List[String]): IO[ExitCode] =
    Blocker[IO].use { blocker =>
      val input = getClass().getClassLoader().getResource("2021-01-15.txt")
      val output = getClass().getClassLoader().getResource("logs.txt")
      println(output.toString)
      io.file
        .readAll[IO](Paths.get(input.toURI), blocker, 4096)
        .through(text.utf8Decode)
        .through(text.lines)
        .filter(_.contains("INFO"))
        .map(_ + "\n")
        .through(text.utf8Encode)
        .through(io.file.writeAll[IO](Paths.get("logs.txt"), blocker, List(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)))
        .compile
        .drain
    }
      .as(ExitCode.Success)
}
