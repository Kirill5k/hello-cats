package io.kirill.hellocats.intro

import java.util.Date

import io.kirill.hellocats.utils.Cat

object Equalities extends App {
  import Cat._

  import cats.Eq
  import cats.instances.int._
  import cats.instances.option._
  import cats.instances.string._
  import cats.instances.long._
  import cats.syntax.eq._
  import cats.syntax.option._

  val eqInt = Eq[Int]
  println(eqInt.eqv(123, 123))
  println(eqInt.eqv(123, 321))
  println(123 === 123)
//  println(123 === "123") // compilation error
//  println(eqInt.eqv(123, "123")) // compilation error

  println((Some(1): Option[Int]) =!= (None: Option[Int]))
  println(Option(1) =!= Option.empty[Int])
  println(1.some =!= none[Int])

  implicit val dateEq: Eq[Date] = Eq.instance{ (d1, d2) => d1.getTime === d2.getTime }
  implicit val catEq: Eq[Cat] = Eq.instance { (c1, c2) => (c1.name === c2.name) && (c1.color === c2.color) && (c1.age === c2.age) }

  println(garfield =!= heathcliff)
  println(Option(garfield) =!= Option.empty[Cat])
}
