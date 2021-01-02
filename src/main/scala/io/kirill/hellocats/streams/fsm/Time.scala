package io.kirill.hellocats.streams.fsm

import cats.effect.Sync

import java.time.Instant

trait Time[F[_]] {
  def now: F[Instant]
}

object Time {
  def apply[F[_]](implicit ev: Time[F]): Time[F] = ev

  implicit def syncInstance[F[_]: Sync]: Time[F] =
    new Time[F] {
      def now: F[Instant] = Sync[F].delay(Instant.now())
    }
}
