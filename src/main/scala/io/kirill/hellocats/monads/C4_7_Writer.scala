package io.kirill.hellocats.monads

object C4_7_Writer extends App {

  import cats.data.Writer
  import cats.instances.vector._
  import cats.syntax.applicative._
  import cats.syntax.writer._

  type Logged[A] = Writer[Vector[String], A]

  println(123.pure[Logged])
  println(123.writer(Vector("log1", "log2", "log3")))
  println(Vector("log1", "log2", "log3").tell)
  println(Writer(Vector("log1", "log2", "log3"), 123))

  val w = Writer(Vector("log1", "log2", "log3"), 123)
  println(w.written)
  println(w.value)
  println(w.run)

  val w2 = for {
    a <-  10.pure[Logged]
    _ <- Vector("a", "b", "c").tell
    b <- 32.writer(Vector("x", "y", "z"))
  } yield a + b
  println(w2)


  def slowly[A](body: => A) = try body finally Thread.sleep(100)

  def factorial(n: Int): Int = {
    val ans = slowly(if(n==0) 1 else n*factorial(n-1))
    println(s"!$n=$ans")
    ans
  }

  def factorialViaWriter(n: Int): Logged[Int] = {
    for {
      ans <- if (n==0) 1.pure[Logged] else slowly(factorialViaWriter(n-1).map(_ * n))
      _ <- Vector(s"!$n=!ans").tell
    } yield ans
  }

  println(factorialViaWriter(5))
}
