name := "hello-cats"

version := "0.1"

scalaVersion := "2.13.1"

lazy val scalaTestVersion = "3.0.8"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.1.0",
  "org.typelevel" %% "cats-effect" % "2.1.1",

  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)

scalacOptions += "-language:higherKinds"