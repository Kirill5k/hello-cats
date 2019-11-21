package io.kirill.hellocats.monads

import cats.data.EitherT

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object C5_Compositions extends App {

  import cats.Monad
  import cats.data.{OptionT, EitherT, Writer} // EitherT, ReaderT, WriterT, StateT, IdT
  import cats.instances.list._ // for Monad
  import cats.instances.future._ // for Monad
  import cats.instances.either._ // for Monad
  import cats.syntax.applicative._ // for pure
  import cats.syntax.either._ // for pure
  import cats.syntax.option._

  type ListOption[A] = OptionT[List, A] // List[Option[A]] transform

  val res1: ListOption[Int] = OptionT(List(10.some))
  val res2: ListOption[Int] = 32.pure[ListOption] // List(Some(32))
  val res3 = res1.flatMap(x => res2.map(y => x +y))
  println(res3)


  type ErrorOr[A] = Either[String, A]
  type ErrorOrOption[A] = OptionT[ErrorOr, A]
  val a = 10.pure[ErrorOrOption]
  val b = 32.pure[ErrorOrOption]
  val c = a.flatMap(x => b.map(y => x + y))
  println(c)


  sealed abstract class HttpError
  final case class NotFound(item: String) extends HttpError
  final case class BadRequest(msg: String) extends HttpError
  type FutureEither[A] = EitherT[Future, HttpError, A]


  type Logged[A] = Writer[List[String], A]
  def parseNumber(str: String): Logged[Option[Int]] =
    util.Try(str.toInt).toOption match {
      case Some(num) => Writer(List(s"read $str"), Some(num))
      case None => Writer(List(s"failed on $str"), None)
    }

  def addAll(a: String, b: String, c: String): Logged[Option[Int]] = {
    val result = for {
      x <- OptionT(parseNumber(a))
      y <- OptionT(parseNumber(b))
      z <- OptionT(parseNumber(c))
    } yield x + y + z
    result.value
  }
  println(addAll("1", "2", "3"))
  println(addAll("1", "a", "3"))



  type Response[A] = EitherT[Future, String, A]
  val powerLevels = Map("jazz" -> 6, "bumblebee" -> 8, "hot-rod" -> 10)
  def getPowerLevel(autobot: String): Response[Int] =
    powerLevels.get(autobot) match {
      case Some(powerlevel) => EitherT.right(Future(powerlevel))
      case None => EitherT.left(Future(s"couldnt find powerlevel of $autobot"))
    }

  def canSpecialMove(ally1: String, ally2: String): Response[Boolean] = {
    for {
      a1 <- getPowerLevel(ally1)
      a2 <- getPowerLevel(ally2)
    } yield (a1 + a2) > 15
  }

  def tacticalReport(ally1: String, ally2: String): String = {
    val stack = canSpecialMove(ally1, ally2).value
    Await.result(stack, 1 second) match {
      case Left(msg) => s"comms error: $msg"
      case Right(true) => s"$ally1 and $ally2 are good"
      case Right(false) => s"$ally1 and $ally2 are not strong enought"
    }
  }

  println(tacticalReport("hot-rod", "jazz"))
  println(tacticalReport("hot-rod", "optimus"))
}
