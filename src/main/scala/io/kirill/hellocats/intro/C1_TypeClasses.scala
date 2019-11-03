package io.kirill.hellocats.intro

import java.util.Date

import io.kirill.hellocats.utils.Cat

final case class Person(name: String, email: String)

object Json {
  def toJson[A](value: A)(implicit w: JsonWriter[A]): Json = w.write(value)
}

sealed trait Json
final case class JsObject(get: Map[String, Json]) extends Json
final case class JsString(get: String) extends Json
final case class JsNumber(get: Double) extends Json
case object JsNull extends Json

trait JsonWriter[A] {
  def write(value: A): Json
}

object Printable {
  def format[A](value: A)(implicit p: Printable[A]): String = p.format(value)
  def print[A](value: A)(implicit p: Printable[A]): Unit = println(p.format(value))
}

trait Printable[A] {
  def format(value: A): String
}

object PrintableInstances {
  implicit val stringFormatter: Printable[String] = (value: String) => value
  implicit val intFormatter: Printable[Int] = (value: Int) => value.toString
  implicit val catFormatter: Printable[Cat] = (value: Cat) => {
    s"${Printable.format(value.name)} is a ${Printable.format(value.age)} years old ${Printable.format(value.color)} cat"
  }
}

object PrintableSyntax {
  implicit class PrintableOps[A](value: A) {
    def format(implicit p: Printable[A]): String = Printable.format(value)
    def print(implicit p: Printable[A]): Unit = Printable.print(value)
  }
}

object TypeClasses extends App {

  implicit val stringWriter: JsonWriter[String] = new JsonWriter[String] {
    override def write(value: String): Json = JsString(value)
  }

  implicit val personWriter: JsonWriter[Person] = new JsonWriter[Person] {
    override def write(value: Person): Json = JsObject(Map("name" -> JsString(value.name), "email" -> JsString(value.email)))
  }

  implicit def optionWriter[A](implicit writer: JsonWriter[A]): JsonWriter[Option[A]] = new JsonWriter[Option[A]] {
    override def write(option: Option[A]): Json = option match {
      case Some(value) => writer.write(value)
      case None => JsNull
    }
  }

  implicit class JsonWriterOps[A](value: A) {
    def toJson(implicit w: JsonWriter[A]): Json = w.write(value)
  }

  val bob = Person("bob", "bob@mail.com")
  Json.toJson(bob)
  Json.toJson(Option(bob))
  bob.toJson

  import PrintableInstances._
  import PrintableSyntax._

  import cats.Show
  import cats.instances.int._
  import cats.instances.string._
  import cats.syntax.show._

  val showInt: Show[Int] = Show[Int] // Show.apply[Int]
  val showString: Show[String] = Show[String] // Show.apply[String]
  implicit val dateShow: Show[Date] = Show.show(value => s"${value.getTime}ms since the epoch")
  implicit val catShow: Show[Cat] = Show.show(cat => s"${cat.name.show} is a ${cat.age.show} years old ${cat.color.show} cat")

  val cat = Cat("alice", 1, "grey")
  Printable.print(1)
  cat.print
  println(showInt.show(2), 2.show)
  println(cat.show)
}
