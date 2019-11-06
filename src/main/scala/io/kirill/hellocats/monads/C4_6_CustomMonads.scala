package io.kirill.hellocats.monads

import io.kirill.hellocats.utils.{Branch, Leaf, Tree}
import io.kirill.hellocats.utils.Tree._

object C4_6_CustomMonads extends App {

  import cats.Monad
  import scala.annotation.tailrec


  val optionMonad = new Monad[Option] {
    override def pure[A](a: A): Option[A] = Some(a)
    override def flatMap[A, B](opt: Option[A])(f: A => Option[B]): Option[B] = opt.flatMap(f)
    @tailrec
    override def tailRecM[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] = f(a) match {
      case None => None
      case Some(Left(a1)) => tailRecM(a1)(f)
      case Some(Right(b)) => Some(b)
    }
  }

  val treeMonad = new Monad[Tree] {

    override def flatMap[A, B](fa: Tree[A])(f: A => Tree[B]): Tree[B] = fa match {
      case Leaf(value) => f(value)
      case Branch(left, right) => Branch(flatMap(left)(f), flatMap(right)(f))
    }

    override def tailRecM[A, B](a: A)(f: A => Tree[Either[A, B]]): Tree[B] = flatMap(f(a)) {
      case Left(value) => tailRecM(value)(f)
      case Right(value) => leaf(value)
    }

    override def pure[A](a: A): Tree[A] = leaf(a)
  }
}
