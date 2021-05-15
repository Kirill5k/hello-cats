name := "hello-cats"

version := "0.1"

scalaVersion := "2.13.5"

lazy val scalaTestVersion = "3.2.7"

lazy val circeVersion   = "0.12.3"
lazy val sttpVersion    = "3.3.1"
lazy val monocleVersion = "2.0.3"
lazy val http4sVersion  = "0.21.14"
lazy val mockitoVersion = "1.16.33"
lazy val fs2Version     = "3.0.1"

libraryDependencies ++= Seq(
  compilerPlugin("org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full),
  "co.fs2"                        %% "fs2-core"                       % fs2Version,
  "co.fs2"                        %% "fs2-io"                         % fs2Version,
  "org.http4s"                    %% "http4s-core"                    % http4sVersion,
  "org.http4s"                    %% "http4s-dsl"                     % http4sVersion,
  "org.http4s"                    %% "http4s-client"                  % http4sVersion,
  "org.http4s"                    %% "http4s-server"                  % http4sVersion,
  "org.http4s"                    %% "http4s-blaze-server"            % http4sVersion,
  "org.http4s"                    %% "http4s-circe"                   % http4sVersion,
  "io.circe"                      %% "circe-core"                     % circeVersion,
  "io.circe"                      %% "circe-generic"                  % circeVersion,
  "io.circe"                      %% "circe-parser"                   % circeVersion,
  "com.github.julien-truffaut"    %% "monocle-core"                   % monocleVersion,
  "com.github.julien-truffaut"    %% "monocle-macro"                  % monocleVersion,
  "com.softwaremill.sttp.client3" %% "core"                           % sttpVersion,
  "com.softwaremill.sttp.client3" %% "circe"                          % sttpVersion,
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % sttpVersion,
  "org.scalatest"                 %% "scalatest"                      % scalaTestVersion % Test,
  "org.mockito"                   %% "mockito-scala"                  % mockitoVersion   % Test,
  "org.mockito"                   %% "mockito-scala-scalatest"        % mockitoVersion   % Test,
  "org.typelevel"                 %% "cats-effect-testing-scalatest"  % "1.0.0"          % Test
)
