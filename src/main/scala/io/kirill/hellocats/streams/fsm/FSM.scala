package io.kirill.hellocats.streams.fsm

trait FSM[S, I, O] {
  def run(state: S, input: I): (S, O)
}

