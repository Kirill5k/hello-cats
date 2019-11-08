package io.kirill.hellocats.semigroups

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait MySemigroupal[F[_]] {
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
}

object C6_1_Semigroups extends App {

  import cats.Semigroupal
  import cats.instances.option._
  import cats.syntax.apply._

  val s1 = Semigroupal[Option].product(Some(123), Some("abc"))
  val s2 = Semigroupal[Option].product(None, Some("abc"))
  val s3 = (Option(123), Option("abc")).tupled
  println(s1)
  println(s2)

  import cats.Monoid
  import cats.Monad
  import cats.instances.int._
  import cats.instances.list._
  import cats.instances.string._
  import cats.instances.invariant._
  import cats.instances.future._
  import cats.syntax.semigroup._
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  case class Cat(name: String, yearOfBirth: Int, favoriteFoods: List[String])

  val tupleToCat: (String, Int, List[String]) => Cat = Cat.apply
  val catToTuple: Cat => (String, Int, List[String]) = cat => (cat.name, cat.yearOfBirth, cat.favoriteFoods)
  implicit val catMonoid: Monoid[Cat] = (Monoid[String], Monoid[Int], Monoid[List[String]]).imapN(tupleToCat)(catToTuple)

  val garfield   = Cat("Garfield", 1978, List("Lasagne"))
  val heathcliff = Cat("Heathcliff", 1988, List("Junk Food"))

  println(garfield |+| heathcliff)

  def product[M[_]: Monad, A, B](x: M[A], y: M[B]): M[(A, B)] = {
    for {
      a <- x
      b <- y
    } yield (a, b)
  }
}
