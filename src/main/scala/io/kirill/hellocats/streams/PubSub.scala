package io.kirill.hellocats.streams

import scala.concurrent.duration._
import cats.implicits._

import java.util.concurrent.TimeUnit
import cats.effect.{Async, ExitCode, IO, IOApp, Sync, Temporal}
import cats.syntax.all._
import fs2.{Pipe, Stream}
import fs2.concurrent.{SignallingRef, Topic}

import java.time.Instant

sealed trait Event
case class Text(value: String) extends Event
case object Quit               extends Event

final class EventService[F[_]](
    eventsTopic: Topic[F, Event],
    interrupter: SignallingRef[F, Boolean]
)(implicit
  F: Async[F]
) {

  private val eventsStream = Stream
    .repeatEval(F.realTime.map(t => Text(Instant.ofEpochMilli(t.toMillis).toString)))
    .metered(1.second)

  // Publishing 15 text events, then single Quit event, and still publishing text events
  def startPublisher: Stream[F, Unit] = {
    val textEvents = eventsTopic.publish(eventsStream)
    val quitEvent = Stream.eval(eventsTopic.publish1(Quit)).drain

    (textEvents.take(15) ++ quitEvent ++ textEvents).interruptWhen(interrupter)
  }

  def startSubscribers: Stream[F, Unit] = {
    def processEvent(subscriberNumber: Int): Pipe[F, Event, Unit] =
      _.flatMap {
        case Text(text) =>
          Stream.eval(F.delay(println(s"Subscriber #$subscriberNumber processing event: $text")))
        case Quit =>
          Stream.eval(interrupter.set(true))
      }

    val events: Stream[F, Event] =
      eventsTopic.subscribe(10)

    Stream(
      events.through(processEvent(1)),
      events.delayBy(5.second).through(processEvent(2)),
      events.delayBy(10.second).through(processEvent(3))
    ).parJoin(3)
  }
}

object PubSub extends IOApp {

  val program = for {
    topic  <- Stream.eval(Topic[IO, Event]).evalTap(_.publish1(Text("Initial Event")))
    signal <- Stream.eval(SignallingRef[IO, Boolean](false))
    service = new EventService[IO](topic, signal)
    _ <- service.startPublisher.concurrently(service.startSubscribers)
  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    program.compile.drain.as(ExitCode.Success)
}
