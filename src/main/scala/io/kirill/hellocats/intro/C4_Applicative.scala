package io.kirill.hellocats.intro

object C4_Applicative extends App {

  import cats.Apply
  import cats.implicits._

  val intToString: Int => String = _.toString
  val double: Int => Int = _ * 2
  val addTwo: Int => Int = _ + 2

  println(Apply[Option].map(Some(1))(intToString))
  println(Apply[Option].ap(Some(intToString))(Some(1)))

  case class Person(name: String, surname: String)

  println(Apply[Option].ap2(Some[(String, String) => Person](Person.apply))(Some("Donald"), Some("Trump")))
  println(Apply[Option].ap2(Some[(String, String) => Person](Person.apply))(None, Some("Trump")))

  println(Apply[Option].tuple2(Some("Donald"), Some("Trump")))
  println(Apply[Option].map2(Some("Donald"), Some("Trump"))(Person.apply))

}
