package io.kirill.hellocats.monads

import cats.data.State
import cats.implicits._

object C4_9_State_2 extends App {

  sealed trait Command
  final case class Damage(n: Int) extends Command
  final case class Heal(n: Int) extends Command

  def update[S](f: S => S): State[S, Unit] = for {
    v <- State.get[S]
    nv = f(v)
    _ <- State.set(nv)
  } yield ()

  def report: State[Int, String] =
    State.get[Int].map(h => s"the current health is $h")

  def runCommand(c: Command): State[Int, Unit] = c match {
    case Damage(n) => update[Int](_ - n)
    case Heal(n) => update[Int](_ + n)
  }

  val commands = List(Damage(1), Damage(5), Heal(2))

  val res = commands.traverse { c =>
    runCommand(c) >> report
  }.runA(10)

  println(res)
}
