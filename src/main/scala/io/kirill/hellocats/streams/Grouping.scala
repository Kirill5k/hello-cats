package io.kirill.hellocats.streams

import cats.effect.std.Queue
import cats.implicits._
import cats.effect.{Concurrent, Ref}
import fs2.{Pipe, Stream}

object Grouping {

  /** Helper flow to group elements of a stream into K substreams.
    * Grows with the number of distinct 'K' selectors
    *
    * Start with an empty Map of keys to queues
    * On element received, invoke the selector function to yield the key denoting which queue this element belongs to
    * If we already have an existing queue for that respective key, append the element lifted in an Option to the queue
    * If a queue for that key does not exist, create a new queue, append it to the queue mapping, and then enqueue the element lifted in an Option
    * For each queue, drain the queue yielding a stream of elements
    * After the stream has been emptied, enqueue a single None to the queue so that the stream halts
    *
    *  @tparam F effect type of the fs2 stream
    *  @param selector partitioning function based on the element
    *  @return a FS2 pipe producing a new sub-stream of elements grouped by the selector
    */
  def groupBy[F[_], A, K](
      selector: A => F[K]
  )(implicit
      F: Concurrent[F]
  ): Pipe[F, A, (K, Stream[F, A])] = { in =>
    Stream
      .eval(Ref.of[F, Map[K, Queue[F, Option[A]]]](Map.empty))
      .flatMap { queuesRef =>
        val cleanup = queuesRef.get.flatMap(_.values.toList.traverse_(_.offer(None)))

        in
          .evalMap(v => selector(v).map(k => (k, v)))
          .evalMap { case (k, v) =>
            for {
              queues <- queuesRef.get
              res <- queues.get(k) match {
                case Some(q) => q.offer(Some(v)).as(none[(K, Stream[F, A])])
                case None => Queue
                  .unbounded[F, Option[A]]
                  .flatTap(_.offer(Some(v)))
                  .flatTap(q => queuesRef.update(_ + (k -> q)))
                  .map(q => (k -> Stream.fromQueueNoneTerminated(q)).some)
              }
            } yield res
          }
          .unNone
          .onFinalize(cleanup)
    }
  }
}
