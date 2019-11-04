package io.kirill.hellocats.intro

trait MyFunctor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}


object Functors extends App {
  import cats.Functor
  import cats.instances.function._
  import cats.instances.list._
  import cats.instances.option._
  import cats.syntax.functor._

  val func1: Int => Double = x => x.toDouble
  val func2: Double => Double = x => x * 2

  println((func1 map func2)(1))
  println((func1 andThen func2)(1))

  val list = List(1, 2, 3)
  val list2 = Functor[List].map(list)(_  *2)

  val lifedFunc1 = Functor[Option].lift(func1)
  println(lifedFunc1(Option(1)))

  def doMath[F[_]](start: F[Int])(implicit functor: Functor[F]): F[Int] = start.map(n => n + 1 * 2)
  println(doMath(Option(10)))

  implicit val optionFunctor: Functor[Option] = new Functor[Option] {
    override def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map(f)
  }

  sealed trait Tree[+A]
  final case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
  final case class Leaf[A](value: A) extends Tree[A]

  implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
    override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa match {
      case Leaf(value) => Leaf(f(value))
      case Branch(left, right) => Branch(map(left)(f), map(right)(f))
    }
  }
}
