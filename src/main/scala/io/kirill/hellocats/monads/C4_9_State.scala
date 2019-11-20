package io.kirill.hellocats.monads

import cats.data.State
import io.kirill.hellocats.monads.C4_9_State.CalcState

object C4_9_State extends App {

  import cats.data.State
  import cats.syntax.applicative._

  val a = State[Int, String] { state =>
    (state, s"The state is $state")
  }

  val (st, res) = a.run(10).value

  // composition
  val st1 = State[Int, String] { num =>
    val ans = num + 1
    (ans, s"result of st1: $ans")
  }

  val st2 = State[Int, String] { num =>
    val ans = num * 2
    (ans, s"result of st2: $ans")
  }

  val both = for {
    a <- st1
    b <- st2
  } yield (a, b)

  println(both.run(10).value)

  val program: State[Int, (Int, Int, Int)] = for {
    a <- State.get[Int] // extracts state as result
    _ <- State.set[Int](a + 1) // updates the state and returns unit as the result
    b <- State.get[Int]
    _ <- State.modify[Int](_ + 1) // updates the state
    c <- State.inspect[Int, Int](_ * 1000) // extracts the state
  } yield (a, b, c)


  type CalcState[A] = State[List[Int], A]

  def evalOne(sym: String): CalcState[Int] =
    sym match {
      case "+" => operation(_ + _)
      case "-" => operation(_ - _)
      case n => appendStack(n.toInt)
    }

  def operation(f: (Int, Int) => Int): CalcState[Int] = State[List[Int], Int] {
    case b :: a :: tail => (f(a, b) :: tail, f(a, b))
    case _ => sys.error("system fail")
  }

  def appendStack(toInt: Int): CalcState[Int] = State[List[Int], Int] {
    state => (toInt :: state, toInt)
  }

  val calc = for {
    _ <- evalOne("5")
    _ <- evalOne("3")
    ans <- evalOne("-")
  } yield ans

  println(calc.run(Nil).value)

  def evalAll(syms: List[String]): CalcState[Int] = syms.foldLeft(0.pure[CalcState])((st, sym) => st.flatMap(_ => evalOne(sym)))
}
