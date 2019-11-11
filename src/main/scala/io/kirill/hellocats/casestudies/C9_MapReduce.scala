package io.kirill.hellocats.casestudies

object C9_MapReduce extends App {

  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global
  import cats.{Monoid, Monad}
  import cats.instances.int._
  import cats.instances.string._
  import cats.instances.list._
  import cats.instances.future._
  import cats.syntax.traverse._
  import cats.syntax.foldable._

  def foldMap[A, B](v: Vector[A])(f: A => B)(implicit m: Monoid[B]): B = v.map(f).foldLeft(m.empty)(m.combine)

  println(foldMap(Vector(1,2,3))(identity))

  def parallelFoldMap[A, B](v: Vector[A])(f: A => B)(implicit m: Monoid[B]): Future[B] = {
    val coresCount = Runtime.getRuntime.availableProcessors
    v.grouped(coresCount)
      .map(Future(_))
      .map(futureBatch => futureBatch.map(foldMap(_)(f)))
      .toList
      .sequence
      .map(result => result.foldLeft(m.empty)(m.combine))
  }

  def parallelFoldMapViaCats[A, B: Monoid](v: Vector[A])(f: A => B): Future[B] = {
    val coresCount = Runtime.getRuntime.availableProcessors

    v.grouped(coresCount)
      .toList
      .traverse(g => Future(g.toList.foldMap(f)))
      .map(_.combineAll)
  }
}
