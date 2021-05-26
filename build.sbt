name := "hello-cats"

version := "0.1"

scalaVersion := "3.0.0"

lazy val sttpVersion    = "3.3.5"
lazy val fs2Version     = "3.0.4"
lazy val monocleVersion = "3.0.0-M6"
lazy val mockitoVersion = "1.16.33"
lazy val scalaTestVersion = "3.2.9"

libraryDependencies ++= Seq(
  "co.fs2"                        %% "fs2-core"                       % fs2Version,
  "co.fs2"                        %% "fs2-io"                         % fs2Version,
  "com.github.julien-truffaut"    %% "monocle-core"                   % monocleVersion,
  "com.github.julien-truffaut"    %% "monocle-macro"                  % monocleVersion,
  "com.softwaremill.sttp.client3" %% "core"                           % sttpVersion,
  "com.softwaremill.sttp.client3" %% "circe"                          % sttpVersion,
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % sttpVersion,
  "org.scalatest"                 %% "scalatest"                      % scalaTestVersion % Test,
//  "org.mockito"                   %% "mockito-scala"                  % mockitoVersion   % Test,
//  "org.mockito"                   %% "mockito-scala-scalatest"        % mockitoVersion   % Test,
  "com.codecommit" %% "cats-effect-testing-scalatest" % "0.5.4"          % Test
)
