package io.kirill.hellocats.monads

import cats.data.EitherT

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object C5_Compositions extends App {

  import cats.Monad
  import cats.syntax.applicative._
  import cats.syntax.flatMap._
  import cats.instances.list._
  import cats.instances.either._
  import cats.instances.future._
  import cats.data.OptionT
  import scala.concurrent.ExecutionContext
  implicit val executor: ExecutionContext =  scala.concurrent.ExecutionContext.global

  type ListOption[A] = OptionT[List, A]

  val a1: ListOption[Int] = OptionT(List(Option(10)))
  val b1: ListOption[Int] = 32.pure[ListOption]

  val c1 = a1.flatMap(x => b1.map(y => x+y))
  println(c1)

  type ErrorOr[A] = Either[String, A]
  type ErrorOrOption[A] = OptionT[ErrorOr, A]

  val a2 = 10.pure[ErrorOrOption]
  val b2 = 32.pure[ErrorOrOption]
  val c2 = a2.flatMap(x => b2.map(y => x+y))
  println(c2) // OptionT(Right(Some(42)))
  println(a2.value)


  type Response[A] = EitherT[Future, String, A]

  val powerLevels = Map("jazz" -> 6, "bumblebee" -> 8, "hot rod" -> 10)

  def getPowerLevel(autobot: String): Response[Int] = {
    powerLevels.get(autobot) match {
      case Some(value) => EitherT.right(Future(value))
      case None => EitherT.left(Future("not found"))
    }
  }

  def canSpecialMove(ally1: String, ally2: String): Response[Boolean] = {
    getPowerLevel(ally1).flatMap(p1 => getPowerLevel(ally2).map(p2 => (p1 + p2) > 15))
  }

  def tactialRepost(ally1: String, ally2: String): String = {
    val stack = canSpecialMove(ally1, ally2).value
    Await.result(stack, 2 seconds) match {
      case Left(msg) => s"comms error: $msg"
      case Right(true) => s"$ally1 and $ally2 can work together"
      case Right(false) => s"$ally1 and $ally2 are no good"
    }
  }
}
