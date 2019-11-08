package io.kirill.hellocats.traversables

object C7_1_Foldables extends App {

  def map[A, B](list: List[A])(f: A => B): List[B] = list.foldLeft(List[B]())((acc, el) => acc :+ f(el))
  def flatMap[A, B](list: List[A])(f: A => List[B]): List[B] = list.foldLeft(List[B]())((acc, el) => acc :++ f(el))
  def sum[A](list: List[A])(implicit numeric: Numeric[A]): A = list.foldLeft(numeric.zero)(numeric.plus)


  import cats.Eval
  import cats.Foldable
  import cats.instances.list._
  import cats.instances.option._
  import cats.instances.int._
  import cats.instances.vector._
  import cats.syntax.foldable._

  val ints = List(1, 2, 3)
  val f1 = Foldable[List].foldLeft(ints, 0)(_ + _)
  println(f1)

  val f2 = Foldable[Option].foldLeft(Option(123), 10)(_ * _)
  println(f2)

  val f3 = Foldable[Option].foldLeft(None: Option[Int], 10)(_ * _)
  println(f3)

  val f4 = Foldable[List].combineAll(List(1,2,3))
  println(f4)

  val moreInts = List(Vector(1,2,3), Vector(4,5,6))
  val f5 = (Foldable[List].compose(Foldable[Vector])).combineAll(moreInts)
  println(f5)
}
