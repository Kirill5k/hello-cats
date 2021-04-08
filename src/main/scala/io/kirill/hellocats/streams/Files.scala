package io.kirill.hellocats.streams

import cats.effect.{IO, IOApp}
import cats.implicits._

object Files extends IOApp.Simple {


  override def run: IO[Unit] = {
    val input = getClass().getClassLoader().getResource("2021-01-15.txt")
    val output = getClass().getClassLoader().getResource("logs.txt")
    IO.println(input.toString) *> IO.println(output.toString)
//    io.file
//      .readAll[IO](Paths.get(input.toURI), 4096)
//      .through(text.utf8Decode)
//      .through(text.lines)
//      .filter(_.contains("INFO"))
//      .map(_ + "\n")
//      .through(text.utf8Encode)
//      .through(io.file.writeAll[IO](Paths.get("logs.txt"), blocker, List(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)))
//      .compile
//      .drain
  }
}
