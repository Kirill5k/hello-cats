package io.kirill.hellocats.streams

import cats.effect.Sync
import cats.implicits._
import fs2.Stream

object Flattening {


  def breadthFirst[F[_]: Sync, E](streams: Stream[F, Stream[F, E]]): Stream[F, Stream[F, E]] =
    Stream.unfoldEval(streams) { streams =>
      val values = streams.flatMap(_.head) // get the head of each stream
      val next = streams.map(_.tail) // continue with the tails
      values.compile.toList.map(_.headOption.map(_ => values -> next))  // stop when there's no more values
    }

}
