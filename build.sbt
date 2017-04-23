import Dependencies._
import CommandExample._

lazy val SlickScaffolder = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "werlang",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    commands ++= Seq(hello),
    name := "SlickScaffolder",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0-M6"
  )
