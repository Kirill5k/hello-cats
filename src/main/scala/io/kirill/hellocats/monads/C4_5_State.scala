package io.kirill.hellocats.monads

object C4_5_State extends App {

  import cats.data.State
  import cats.syntax.applicative._

  val a = State[Int, String] { state =>
    (state, s"the state is $state")
  }

  val (state, result) = a.run(10).value
  println(state, result)

  val state1 = State[Int, String] { num =>
    val ans = num + 1
    (ans, s"result of step1: $ans")
  }

  val state2 = State[Int, String] { num =>
    val ans = num * 2
    (ans, s"result of step2: $ans")
  }

  val both = for {
    a <- state1
    b <- state2
  } yield (a, b)

  println(both.run(20).value)

  val getDemo = State.get[Int] // extracts STATE as result
  println("GET extracts STATE as result", getDemo.run(10).value)

  val setDemo = State.set[Int](30) // updates the state and returns unit as res
  println("SET updates the state and returns unit as res", setDemo.run(10).value)

  val pureDemo = State.pure[Int, String]("Result")
  println("PURE ignores the state and returns result", pureDemo.run(10).value)

  val inspectDemo = State.inspect[Int, String](_ + "!")
  println("INSPECT extracts state via trans function", inspectDemo.run(10).value)

  val modifyDemo = State.modify[Int](_ + 1)
  println("MODIFY updates state using func", modifyDemo.run(10).value)

  import State._

  val program: State[Int, (Int, Int, Int)] = for {
    a <- get[Int]
    _ <- set[Int](a + 1)
    b <- get[Int]
    _ <- modify[Int](_ + 1)
    c <- inspect[Int, Int](_ * 1000)
  } yield (a, b, c)


  type CalcState[A] = State[List[Int], A]

  def evalOne(sym: String): CalcState[Int] = sym match {
    case "+" => operator(_ + _)
    case "-" => operator(_ - _)
    case "*" => operator(_ * _)
    case "/" => operator(_ / _)
    case num => operand(num.toInt)
  }

  def operator(func: (Int, Int) => Int): CalcState[Int] = State[List[Int], Int] {
    case b :: a :: tail =>
      val ans = func(a, b)
      (ans :: tail, ans)
    case _ => sys.error("fail")
  }

  def operand(num: Int): CalcState[Int] = State[List[Int], Int] { stack => (num :: stack, -1)}

  val calculationResult = for {
    _ <- evalOne("1")
    _ <- evalOne("2")
    ans <- evalOne("+")
  } yield ans
  println(calculationResult.runA(Nil).value)

  def evalAll(input: List[String]): CalcState[Int] = input.foldLeft(0.pure[CalcState])((acc, sym) => acc.flatMap(_ => evalOne(sym)))

  println(evalAll(List("1", "2", "+", "39", "+")).runA(Nil).value)
}
