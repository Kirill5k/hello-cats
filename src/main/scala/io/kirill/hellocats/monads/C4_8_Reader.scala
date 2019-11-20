package io.kirill.hellocats.monads

import io.kirill.hellocats.utils.Cat._
import io.kirill.hellocats.utils.Cat

object C4_8_Reader extends App {

  import cats.data.Reader
  import cats.syntax.applicative._

  val catNameReader: Reader[Cat, String] = Reader(cat => cat.name)
  val catColorReader: Reader[Cat, String] = Reader(cat => cat.color)

  println(catNameReader.run(garfield))

  val nameAndColorReader = for {
    n <- catNameReader
    c <- catColorReader
  } yield (n, c)

  println(nameAndColorReader.run(garfield))
  println(nameAndColorReader(garfield)) // same as the previous

  case class Db(username: Map[Int, String], passwords: Map[String, String])
  type DbReader[A] = Reader[Db, A]

  def findUsername(userId: Int): DbReader[Option[String]] = Reader(db => db.username.get(userId))
  def checkPassword(username: String, password: String): DbReader[Boolean] = Reader(db => db.passwords.get(username).contains(password))
  def checkLogin(userId: Int, password: String): DbReader[Boolean] = {
    findUsername(userId).flatMap {
      case None => false.pure[DbReader]
      case Some(username) => checkPassword(username, password)
    }
  }

  val users = Map(1 -> "alice", 2 -> "bob", 3 -> "charlie")
  val passwords = Map("alice" -> "123", "bob" -> "456", "charlie" -> "789")
  val db = Db(users, passwords)

  println(checkLogin(1, "123").run(db))
  println(checkLogin(1, "456").run(db))
}
