package io.kirill.hellocats.intro

trait MySemigroup[A] {
  def combine(x: A, y: A): A
}

trait MyMonoid[A] extends MySemigroup[A] { // must be associative (i.e. x*(y*z) === (x*y)*z)
  def empty: A
}

object MyMonoid {
  def apply[A](implicit monoid: MyMonoid[A]): MyMonoid[A] = monoid
}

object MonoidInstances {
  implicit val booleanAndMonoid: MyMonoid[Boolean] = new MyMonoid[Boolean] {
    override def empty: Boolean = true
    override def combine(x: Boolean, y: Boolean): Boolean = x && y
  }
  implicit val booleanOrMonoid: MyMonoid[Boolean] = new MyMonoid[Boolean] {
    override def empty: Boolean = false
    override def combine(x: Boolean, y: Boolean): Boolean = x || y
  }

  implicit def setUnionMonoid[A]: MyMonoid[Set[A]] = new MyMonoid[Set[A]] {
    override def empty: Set[A] = Set()
    override def combine(x: Set[A], y: Set[A]): Set[A] = x union y
  }

  implicit def setIntersectionSemigroup[A]: MySemigroup[Set[A]] = (x: Set[A], y: Set[A]) => x intersect y
}

object C2_Monoids extends App {

  import cats.Monoid
  import cats.Semigroup
  import cats.instances.string._
  import cats.instances.option._
  import cats.instances.int._
  import cats.syntax.semigroup._

  println(Semigroup[String].combine("Hello, ", "World!"))
  println(Monoid[String].combine("Hello, ", "World!"))
  println(Monoid[Option[Int]].combine(Some(10), Some(90)))

  println(1 |+| 2 |+| Monoid[Int].empty)

  final case class Order(totalCost: Double, quantity: Double)
  implicit val orderMonoid: Monoid[Order] = new Monoid[Order] {
    override def empty: Order = Order(0, 0)
    override def combine(x: Order, y: Order): Order = Order(x.totalCost+y.totalCost, x.quantity+y.quantity)
  }

  def add[A](items: List[A])(implicit monoid: Monoid[A]): A = items.foldLeft(monoid.empty)(monoid.combine)

  println(add(List(1, 2, 3, 4, 5)))
}
