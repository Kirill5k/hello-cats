name := "hello-cats"

version := "0.1"

scalaVersion := "2.13.3"

lazy val scalaTestVersion = "3.2.0"

lazy val circeVersion   = "0.12.3"
lazy val sttpVersion    = "2.0.5"
lazy val monocleVersion = "2.0.3"
lazy val http4sVersion  = "0.21.14"
lazy val mockitoVersion = "1.16.33"

libraryDependencies ++= Seq(
  "co.fs2"                       %% "fs2-core"                       % "2.5.0",
  "org.http4s"                   %% "http4s-core"                    % http4sVersion,
  "org.http4s"                   %% "http4s-dsl"                     % http4sVersion,
  "org.http4s"                   %% "http4s-client"                  % http4sVersion,
  "org.http4s"                   %% "http4s-server"                  % http4sVersion,
  "org.http4s"                   %% "http4s-blaze-server"            % http4sVersion,
  "org.http4s"                   %% "http4s-circe"                   % http4sVersion,
  "io.circe"                     %% "circe-core"                     % circeVersion,
  "io.circe"                     %% "circe-generic"                  % circeVersion,
  "io.circe"                     %% "circe-parser"                   % circeVersion,
  "com.github.julien-truffaut"   %% "monocle-core"                   % monocleVersion,
  "com.github.julien-truffaut"   %% "monocle-macro"                  % monocleVersion,
  "com.softwaremill.sttp.client" %% "core"                           % sttpVersion,
  "com.softwaremill.sttp.client" %% "circe"                          % sttpVersion,
  "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % sttpVersion,
  "org.scalatest"                %% "scalatest"                      % scalaTestVersion % Test,
  "org.mockito"                  %% "mockito-scala"                  % mockitoVersion   % Test,
  "org.mockito"                  %% "mockito-scala-scalatest"        % mockitoVersion   % Test,
  "com.codecommit"               %% "cats-effect-testing-scalatest"  % "0.4.0"          % Test
)

scalacOptions += "-language:higherKinds"
