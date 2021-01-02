name := "hello-cats"

version := "0.1"

scalaVersion := "2.13.3"

lazy val scalaTestVersion = "3.2.0"

lazy val circeVersion   = "0.12.3"
lazy val sttpVersion    = "2.0.5"
lazy val monocleVersion = "2.0.3"

libraryDependencies ++= Seq(
  "co.fs2"                       %% "fs2-core"                       % "2.4.4",
  "io.circe"                     %% "circe-core"                     % circeVersion,
  "io.circe"                     %% "circe-generic"                  % circeVersion,
  "io.circe"                     %% "circe-parser"                   % circeVersion,
  "com.github.julien-truffaut"   %% "monocle-core"                   % monocleVersion,
  "com.github.julien-truffaut"   %% "monocle-macro"                  % monocleVersion,
  "com.softwaremill.sttp.client" %% "core"                           % sttpVersion,
  "com.softwaremill.sttp.client" %% "circe"                          % sttpVersion,
  "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % sttpVersion,
  "org.scalatest"                %% "scalatest"                      % scalaTestVersion % Test,
  "com.codecommit"               %% "cats-effect-testing-scalatest"  % "0.4.0"
)

scalacOptions += "-language:higherKinds"
