package io.kirill.hellocats.usageexamples.happyeyeballs

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration._

class HappyEyeballsSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A HappyEyeballs should" - {

    "return first to resolve task" in {
      val tasks = List(
        normalTask("t1", 10.seconds),
        normalTask("t2", 2.seconds),
        normalTask("t3", 3.seconds),
        normalTask("t4", 4.seconds),
        normalTask("t5", 1.second)
      )

      val winner = HappyEyeballs.run(tasks, 2.seconds)

      winner.asserting(_ mustBe "t2")
    }

    "handle errors" in {
      val tasks = List(
        normalTask("t1", 10.seconds),
        erroneusTask("t2", 2.seconds),
        normalTask("t3", 3.seconds),
        normalTask("t4", 4.seconds),
        normalTask("t5", 1.second)
      )

      val winner = HappyEyeballs.run(tasks, 2.seconds)

      winner.asserting(_ mustBe "t3")
    }
  }

  def normalTask(name: String, duration: FiniteDuration): IO[String] =
   IO(println(s"starting task $name")) *> IO.sleep(duration) *> IO.pure(name)

  def erroneusTask(name: String, duration: FiniteDuration): IO[String] =
    IO(println(s"starting task $name")) *> IO.sleep(duration) *> IO.raiseError(new RuntimeException("uh oh"))
}
