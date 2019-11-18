package io.kirill.hellocats.usageexamples

import scala.util.Try
import cats.implicits._


case class TransformError(message: String)


case class Person(firstName: String, lastName: String)

object Person {
  def fromNameString(fullNameString: String): Either[TransformError, Person] = fullNameString.split(" ") match {
    case Array(firstName, lastName) => Person(firstName, lastName).asRight[TransformError]
    case _ => TransformError("unexpected name format").asLeft[Person]
  }
}


case class PhoneNumber(countryCode: Int, areaCode: Int, prefix: Int, lineNumber: Int) {
  override def toString: String = s"$countryCode ($areaCode) $prefix-$lineNumber"
}

object PhoneNumber {
  private val pattern = """(\d{1})-(\d{3})-(\d{3})-(\d{4})""".r
  private def parseInt(s: String): Either[TransformError, Int] = Try(s.toInt).toEither.leftMap(e => TransformError(e.getMessage))

  def fromPhoneString(phoneString: String): Either[TransformError, PhoneNumber] = phoneString match {
    case pattern(code, area, prefix, line) => (parseInt(code), parseInt(area), parseInt(prefix), parseInt(line)).mapN(PhoneNumber.apply)
    case _ => TransformError("phone string did not match the expected pattern").asLeft[PhoneNumber]
  }
}
