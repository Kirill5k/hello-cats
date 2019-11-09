package io.kirill.hellocats.traversables

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object C7_2_Traversables extends App {

  val hosts = List("google.com", "yahoo.com", "bing.com")

  def getUptime(host: String): Future[Int] = Future(host.length * 60)

  val allUptimes: Future[List[Int]] = hosts.foldLeft(Future(List[Int]()))((acc, host) => {
    for {
      acc <- acc
      uptime <- getUptime(host)
    } yield acc :+ uptime
  })

  val allUptimesViaFuture: Future[List[Int]] = Future.traverse(hosts)(getUptime)


  import cats.Applicative
  import cats.data.Validated
  import cats.instances.future._
  import cats.instances.list._
  import cats.syntax.applicative._
  import cats.syntax.apply._

  def listTravers[F[_]: Applicative, A, B](list: List[A])(f: A => F[B]): F[List[B]] = list.foldLeft(List.empty[B].pure[F]){ (acc, el) =>
    (acc, f(el)).mapN(_ :+ _)
  }

  import cats.Traverse


  val totalUptimeViaCats: Future[List[Int]] = Traverse[List].traverse(hosts)(getUptime)

  val numbers = List(Future(1), Future(2), Future(3))
  val traversedNumbers: Future[List[Int]] = Traverse[List].sequence(numbers)
}
