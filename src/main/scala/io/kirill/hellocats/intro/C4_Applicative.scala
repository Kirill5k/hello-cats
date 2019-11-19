package io.kirill.hellocats.intro


trait Apply[F[_]] {
  def ap[A, B](f: F[A => B])(fa: F[A]): F[B]
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

object ApplyOps {
  implicit val optionApply: Apply[Option] = new Apply[Option] {
    override def ap[A, B](f: Option[A => B])(fa: Option[A]): Option[B] = fa.flatMap(a => f.map(ff => ff(a)))

    override def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa map f
  }
}

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

  println(("Donald".some, "Trump".some).mapN(Person.apply))
  println(("Donald".asRight[Int], "Trump".asRight[Int]).mapN(Person.apply))
}
