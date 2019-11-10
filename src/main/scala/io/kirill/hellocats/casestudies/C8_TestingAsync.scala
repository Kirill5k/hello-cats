package io.kirill.hellocats.casestudies

import scala.concurrent.Future

object C8_TestingAsync extends App {
  import cats.Id
  import cats.Applicative
  import cats.instances.future._
  import cats.instances.list._
  import cats.syntax.traverse._
  import cats.syntax.functor._
  import scala.concurrent.ExecutionContext.Implicits.global

  trait UptimeClient[F[_]] {
    def getUptime(host: String): F[Int]
  }

  trait RealUptimeClient extends UptimeClient[Future] {
    override def getUptime(host: String): Future[Int]
  }

  class TestUptimeClient(hosts: Map[String, Int]) extends UptimeClient[Id] {
    override def getUptime(host: String): Id[Int] = hosts.getOrElse(host, 0)
  }

  class UptimeService[F[_]: Applicative](client: UptimeClient[F]) {
    def getTotalUptime(hosts: List[String]): F[Int] = hosts.traverse(client.getUptime).map(_.sum)
  }
}
