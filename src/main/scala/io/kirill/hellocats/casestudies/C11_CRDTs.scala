package io.kirill.hellocats.casestudies

object C11_CRDTs extends App {

  import cats.kernel.CommutativeMonoid
  import cats.instances.map._
  import cats.instances.list._
  import cats.syntax.semigroup._
  import cats.syntax.foldable._

  trait BoundedSemiLattice[A] extends CommutativeMonoid[A] {
    def combine(a1: A, a2: A): A
    def empty: A
  }

  object BoundedSemiLattice {
    implicit val intInstance: BoundedSemiLattice[Int] = new BoundedSemiLattice[Int] {
      override def combine(a1: Int, a2: Int): Int = a1 max a2
      override def empty: Int = 0
    }

    implicit def setInstance[A]: BoundedSemiLattice[Set[A]] = new BoundedSemiLattice[Set[A]] {
      override def combine(a1: Set[A], a2: Set[A]): Set[A] = a1 union a2
      override def empty: Set[A] = Set[A]()
    }
  }


  trait GCounter[F[_, _], K, V] {
    def increment(f: F[K, V])(k: K, v: V)(implicit m: CommutativeMonoid[V]): F[K, V]
    def merge(f1: F[K, V], f2: F[K, V])(implicit b: BoundedSemiLattice[V]): F[K, V]
    def total(f: F[K, V])(implicit m: CommutativeMonoid[V]): V
  }

  object GCounter {
    def apply[F[_, _], K, V](implicit counter: GCounter[F, K, V]) = counter

    implicit def mapInstance[K, V]: GCounter[Map, K, V] = new GCounter[Map, K, V] {
      override def increment(f: Map[K, V])(k: K, v: V)(implicit m: CommutativeMonoid[V]): Map[K, V] = {
        val total = f.getOrElse(k, m.empty) |+| v
        f + (k -> total)
      }

      override def merge(f1: Map[K, V], f2: Map[K, V])(implicit b: BoundedSemiLattice[V]): Map[K, V] = {
        f1 |+| f2
      }

      override def total(f: Map[K, V])(implicit m: CommutativeMonoid[V]): V = f.values.toList.combineAll
    }
  }
}
