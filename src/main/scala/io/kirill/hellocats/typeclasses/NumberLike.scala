package io.kirill.hellocats.typeclasses


final case class StringNumber(n: String)


trait Number[A] {
  def sum(a: A, b: A): A
}

object Number {
  implicit def stringNumber: Number[StringNumber] = new Number[StringNumber] {
    override def sum(a: StringNumber, b: StringNumber): StringNumber =
      StringNumber((a.n.toInt + b.n.toInt).toString)
  }

  def apply[A](implicit ev: Number[A]): Number[A] = ev
}

object NumberLike {

  implicit class NumberOps[A](a: A)(implicit N: Number[A]) {
    def +(b: A): A = N.sum(a, b)
  }

  def addTwo[A: Number](a: A, b: A): A = a.+(b)
}
