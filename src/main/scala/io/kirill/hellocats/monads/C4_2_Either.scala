package io.kirill.hellocats.monads

object C4_2_Either extends App {

  import cats.syntax.either._

  val a = 3.asRight[String] //Either[String, Int]
  val b = 4.asRight[String]

  for {
    x <- a
    y <- b
  } println(x*x + y*y)

  def countPositive(nums: List[Int]): Either[String, Int] =
    nums.foldLeft(0.asRight[String]){ (acc, num) => if (num > 0) acc.map(_ + 1) else Left("Negative.")}

  println(countPositive(List(1, 2, 3)))
  println(countPositive(List(1, -2, 3)))

  println(Either.catchOnly[NumberFormatException]("foo".toInt))
  // println(Either.catchOnly[NumberFormatException](1/0)) // will throw
  println(Either.fromTry(scala.util.Try("foo".toInt)))

  "Error".asLeft[Int].getOrElse(0) // 0
  (-1).asRight[String].ensure("must be non-negative")(_ > 0)

  val result = for {
    a <- 1.asRight[String]
    b <- 0.asRight[String]
    c <- if (b==0) "DIV0".asLeft[Int] else (a/b).asRight[String]
  } yield c * 100
  println(result)
}
