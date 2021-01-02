package io.kirill.hellocats.streams.fsm

trait FSM[F[_], S, I, O] {
  def run(state: S, input: I): F[(S, O)]
}

