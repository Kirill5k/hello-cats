name := "hello-cats"

version := "0.1"

scalaVersion := "2.13.1"

lazy val scalaTestVersion = "3.1.1"

lazy val circeVersion = "0.12.3"
lazy val sttpVersion = "2.0.5"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.1.0",
  "org.typelevel" %% "cats-effect" % "2.1.1",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "com.softwaremill.sttp.client" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client" %% "circe" % sttpVersion,
  "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % sttpVersion,

  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "com.codecommit" %% "cats-effect-testing-scalatest" % "0.4.0"
)

scalacOptions += "-language:higherKinds"