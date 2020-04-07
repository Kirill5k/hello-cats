name := "hello-cats"

version := "0.1"

scalaVersion := "2.13.1"

lazy val scalaTestVersion = "3.1.1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.1.0",
  "org.typelevel" %% "cats-effect" % "2.1.1",

  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "com.codecommit" %% "cats-effect-testing-scalatest" % "0.4.0"
)

scalacOptions += "-language:higherKinds"