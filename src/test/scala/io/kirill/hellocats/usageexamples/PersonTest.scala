package io.kirill.hellocats.usageexamples

import org.scalatest.{FunSpec, FunSuite, Matchers}

class PersonTest extends FunSpec with Matchers {

  describe("person") {
    describe("from string") {
      it("should create person from string") {
        val result = Person.fromNameString("Boris Johnson")
        result should be (Right(Person("Boris", "Johnson")))
      }

      it("should return transform error when string is invalid") {
        val result = Person.fromNameString("foo-bar")
        result should be (Left(TransformError("unexpected name format")))
      }
    }
  }

  describe("phone number") {
    describe("from String") {
      it("should parse phone number from String") {
        val result = PhoneNumber.fromPhoneString("1-715-210-8222")
        result should be (Right(PhoneNumber(1, 715, 210, 8222)))
      }

      it("should return transform error when string is invalid") {
        val result = PhoneNumber.fromPhoneString("foo-bar")
        result should be (Left(TransformError("phone string did not match the expected pattern")))
      }
    }
  }

}
