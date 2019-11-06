package io.kirill.hellocats.monads

import io.kirill.hellocats.utils.Cat._
import io.kirill.hellocats.utils.Cat

object C4_4_Readers extends App {
  import cats.data.Reader

  val catNameReader: Reader[Cat, String] = Reader(cat => cat.name)

  println(catNameReader.run(garfield))

  val greetCat: Reader[Cat, String] = catNameReader.map(name => s"Hello, $name!")
  val catAgeReader: Reader[Cat, String] = Reader(cat => s"you are ${cat.age} years old")

  val greetAndGetReadAge: Reader[Cat, String] = for {
    greet <- greetCat
    age <- catAgeReader
  } yield s"$greet, $age"

  println(greetAndGetReadAge.run(heathcliff))

  case class Db(username: Map[Int, String], password: Map[String, String])

  type DbReader[A] = Reader[Db, A]

  def findUsername(userId: Int): DbReader[Option[String]] = Reader(db => db.username.get(userId))
  def checkPassword(user: String, pass: String): DbReader[Boolean] = Reader(db => db.password.get(user).contains(pass))
  def checkLogin(userId: Int, pass: String): DbReader[Boolean] = Reader(db => db.password.get(db.username(userId)).contains(pass))

}
