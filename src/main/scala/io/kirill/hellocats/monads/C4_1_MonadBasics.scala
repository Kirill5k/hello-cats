package io.kirill.hellocats.monads

trait MyMonad[F[_]] {
  def pure[A](value: A): F[A]
  def flatMap[A, B](value: F[A])(func: A => F[B]): F[B]

  def map[A, B](value: F[A])(func: A => B): F[B] = flatMap(value)(a => pure(func(a)))
}

object C4_1_MonadBasics extends App {
  import cats.Id
  import cats.Monad
  import cats.instances.option._
  import cats.instances.list._
  import cats.syntax.applicative._
  import cats.syntax.functor._
  import cats.syntax.flatMap._

  val opt1 = Monad[Option].pure(3)
  val opt2 = Monad[Option].flatMap(opt1)(a => Some(a+2))
  val opt3 = Monad[Option].map(opt2)(a => 100 * 2)
  println(opt3)

  val list1 = Monad[List].pure(3)
  val list2 = Monad[List].flatMap(List(1, 2, 3))(a => List(a, a*10))
  val list3 = Monad[List].map(list2)(a => a + 123)
  println(list3)

  println(1.pure[Option])

  def sumSquare[F[_]: Monad](a: F[Int], b: F[Int]): F[Int] = a.flatMap(x => b.map(y => x*x + y*y))
  def sumSquareViaFor[F[_]: Monad](a: F[Int], b: F[Int]): F[Int] = {
    for {
      x <- a
      y <- b
    } yield x*x + y*y
  }

  println(sumSquare(Option(3), Option(4)))
  println(sumSquare(List(3), List(4)))
  println(sumSquare(3: Id[Int], 4: Id[Int]))

  val aId = Monad[Id].pure(3)
  val bId = Monad[Id].flatMap(aId)(_ + 1)

  def pureId[A](value: A): Id[A] = value: Id[A]
  def mapId[A, B](initial: Id[A])(f: A => B): Id[B] = f(initial): Id[B]
  def flatMapId[A, B](initial: Id[A])(f: A => Id[B]): Id[B] = f(initial): Id[B]
}
