package io.kirill.hellocats.intro

object C2_Semigroups extends App {

  import cats.Semigroup
  import cats.implicits._

  println(Semigroup[Option[Int]].combine(Option(1), None))
  println(Semigroup[List[_]].combine(List(1, 2, 3), List("foo", "bar")))

  val f = Semigroup[Int => Int].combine(_ + 1, _ * 10)
  println(f(6))

  println(Semigroup[Option[String]].combine("donald".some, "trump". some))
  println("donald".some |+| " ".some |+| "trump".some)
}
