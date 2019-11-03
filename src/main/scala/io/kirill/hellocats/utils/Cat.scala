package io.kirill.hellocats.utils

final case class Cat(name: String, age: Int, color: String)

object Cat {
  val garfield = Cat("Garfield", 38, "orange and black")
  val heathcliff = Cat("Heathcliff", 33, "orange and black")
}
