package io.kirill.hellocats.streams

import cats.effect.Sync

import java.time.LocalTime

object utils {

  def putStr[F[_]: Sync](str: Any): F[Unit] =
    Sync[F].delay(println(str.toString))

  def log[F[_]: Sync](message: String): F[Unit] =
    Sync[F].delay(println(s"${LocalTime.now()}: $message"))

  def appendLog[F[_]: Sync](message: String): F[Unit] =
    Sync[F].delay(print(message))
}
