package io.kirill.hellocats.intro

import io.kirill.hellocats.utils.{Branch, Leaf, Tree}

trait MyFunctor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}


object Functors extends App {
  import cats.Functor
  import cats.instances.function._
  import cats.instances.list._
  import cats.instances.option._
  import cats.syntax.functor._

  val func1: Int => Double = x => x.toDouble
  val func2: Double => Double = x => x * 2

  println((func1 map func2)(1))
  println((func1 andThen func2)(1))

  val list = List(1, 2, 3)
  val list2 = Functor[List].map(list)(_  *2)

  val lifedFunc1 = Functor[Option].lift(func1)
  println(lifedFunc1(Option(1)))

  def doMath[F[_]](start: F[Int])(implicit functor: Functor[F]): F[Int] = start.map(n => n + 1 * 2)
  println(doMath(Option(10)))

  implicit val optionFunctor: Functor[Option] = new Functor[Option] {
    override def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map(f)
  }

  implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
    override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa match {
      case Leaf(value) => Leaf(f(value))
      case Branch(left, right) => Branch(map(left)(f), map(right)(f))
    }
  }

  trait Printable[A] { self =>
    def format(value: A): String
    def contramap[B](f: B => A): Printable[B] = new Printable[B] {
      override def format(value: B): String = self.format(f(value))
    }
  }

  def format[A](value: A)(implicit p: Printable[A]): String = p.format(value)

  implicit val stringPrintable: Printable[String] = (value: String) => "\"" + value +  "\""
  implicit val booleanPrintable: Printable[Boolean] = (value: Boolean) => if (value) "yes" else "no"

  println(format("hello"))
  println(format(true))

  final case class Box[A](value: A)
  implicit def boxPrintable[A](implicit p: Printable[A]): Printable[Box[A]] = p.contramap(_.value)

  println(format(Box(false)))

  trait Codec[A] { self =>
    def encode(value: A): String
    def decode(value: String): A
    def imap[B](dec: A => B, enc: B => A): Codec[B] = new Codec[B] {
      override def encode(value: B): String = self.encode(enc(value))
      override def decode(value: String): B = dec(self.decode(value))
    }
  }

  def encode[A](value: A)(implicit c: Codec[A]): String = c.encode(value)
  def decode[A](value: String)(implicit c: Codec[A]): A = c.decode(value)

  implicit val stringCodec: Codec[String] = new Codec[String] {
    override def encode(value: String): String = value
    override def decode(value: String): String = value
  }

  implicit val intCodec: Codec[Int] = stringCodec.imap(_.toInt, _.toString)
  implicit val booleanCodec: Codec[Boolean] = stringCodec.imap(_.toBoolean, _.toString)
  implicit val doubleCodec: Codec[Double] = stringCodec.imap(_.toDouble, _.toString)
  implicit def boxCodec[A](implicit c: Codec[A]): Codec[Box[A]] = stringCodec.imap(str => Box(c.decode(str)), b => c.encode(b.value))

  import cats.Contravariant
  import cats.Show
  import cats.instances.string._

  val showString = Show[String]
  val showSymbol = Contravariant[Show].contramap(showString)((sym: Symbol) => s"'${sym.name}")


  val stringList = List("foo", "bar")
  val transform: String => Int = _.length
  val liftedTransform: List[String] => List[Int] = Functor[List].lift(transform)
  println(liftedTransform(stringList))

  val product = Functor[List].fproduct(stringList)(transform).toMap
  println(product)
}
