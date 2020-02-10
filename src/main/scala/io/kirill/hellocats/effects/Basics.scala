package io.kirill.hellocats.effects

import cats.effect.{IO, Resource}
import cats.implicits._
import java.io._


object Basics extends App {

  def inputStream(f: File): Resource[IO, FileInputStream] =
    Resource.make { IO(new FileInputStream(f)) } { inStream =>
      IO(inStream.close()).handleErrorWith(_ => IO.unit)
    }

  def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.make { IO(new FileOutputStream(f)) } { outStream =>
      IO(outStream.close()).handleErrorWith(_ => IO.unit)
    }

  def inputOutputStream(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
    for {
      inStream <- inputStream(in)
      outStream <- outputStream(out)
    } yield (inStream, outStream)
}
