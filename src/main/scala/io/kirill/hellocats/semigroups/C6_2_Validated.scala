package io.kirill.hellocats.semigroups

object C6_2_Validated extends App {

  import cats.Semigroupal
  import cats.data.Validated
  import cats.instances.list._
  import cats.instances.string._
  import cats.syntax.apply._
  import cats.syntax.validated._
  import cats.syntax.either._
  import cats.syntax.applicative._
  import cats.syntax.applicativeError._

  type AllErrorsOr[A] = Validated[List[String], A]
  val e = Semigroupal[AllErrorsOr].product(Validated.invalid(List("error 1")), Validated.invalid(List("error 2")))
  println(e)

  val e1 = Validated.catchOnly[NumberFormatException]("foo".toInt)
  println(e1)

  val e2 = Validated.catchNonFatal(sys.error("madness"))
  println(e2)

  val e3 = Validated.fromOption[String, Int](None, "madness")
  println(e3)

  val e4 = "maddness".invalid[Int].toEither
  println(e4)
  println(e4.toValidated)

  val e5 = "fail".invalid[Int].getOrElse(0)
  println(e5)

  val e6 = "fail".invalid[Int].fold(_ + "!!!", _.toString)
  println(e6)


  case class User(name: String, age: Int)

  type FormData = Map[String, String]
  type FailFast[A] = Either[List[String], A]
  type FailSlow[A] = Validated[List[String], A]


  def readValue(key: String, form: FormData): FailFast[String] =
    Either.fromOption[List[String], String](form.get(key), List(s"$key is not provided"))

  def parseInt(string: String): FailFast[Int] =
    Either.catchOnly[NumberFormatException](string.toInt).leftMap(_ => List(s"$string is not a number"))

  def nonBlank(string: String): FailFast[String] = Right(string).ensure(List("field cannot be empty"))(_.nonEmpty)
  def nonNegative(int: Int): FailFast[Int] = Right(int).ensure(List("must be greater than 0"))(_ >= 0)

  def readName(form: FormData): Either[List[String], String] = readValue("name", form).flatMap(nonBlank)
  def readAge(form: FormData): Either[List[String], Int] =
    readValue("age", form)
      .flatMap(nonBlank)
      .flatMap(parseInt)
      .flatMap(nonNegative)

  def readUser(data: FormData): FailSlow[User] = (readName(data).toValidated, readAge(data).toValidated).mapN(User.apply)
}
