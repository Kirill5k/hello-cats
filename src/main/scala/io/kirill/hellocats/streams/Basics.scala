package io.kirill.hellocats.streams

import cats.effect.{ExitCode, IO, IOApp}

import scala.concurrent.duration._

object Basics extends IOApp.Simple {

  override def run: IO[Unit] = {
    val st1 = fs2.Stream.eval(IO.pure(1)).metered(2.second).repeat
    val st2 = fs2.Stream.eval(IO.pure(2)).metered(2.second).repeat

    st2.hold(0).compile.resource

    val res = st1.merge(st2).interruptAfter(10.second).compile.toList

    res.flatMap(r => IO(println(r)))
  }
}
