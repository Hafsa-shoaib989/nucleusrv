// See README.md for license details.

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.13.0")
    }
  }
}

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

ThisBuild / version := "3.2.0"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / crossScalaVersions := Seq("2.12.10", "2.11.12")
ThisBuild / Test / logBuffered := false
ThisBuild / Test / parallelExecution := false
ThisBuild / trapExit := false

val defaultVersions = Map(
  "chisel3" -> "3.5.5",
  "chisel-iotesters" -> "2.5.6"
)

// Subproject: berkeley-hardfloat
lazy val hardfloat = project in file("berkeley-hardfloat")

// Subproject: vaquita (depends on hardfloat)
lazy val vaquita = (project in file("vaquita")).dependsOn(hardfloat)

// Root project: nucleusrv (depends on vaquita)
lazy val root = (project in file(".")).settings(
  name := "nucleusrv",
  libraryDependencies ++= Seq("chisel3", "chisel-iotesters").map {
    dep => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep))
  } ++ Seq(
    "edu.berkeley.cs" %% "chiseltest" % "0.5.6" % Test,
    "org.scalatest" %% "scalatest" % "3.2.0" % Test
  ),
  addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % defaultVersions("chisel3") cross CrossVersion.full),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
  ),
  scalacOptions ++= scalacOptionsVersion(scalaVersion.value),
  javacOptions ++= javacOptionsVersion(scalaVersion.value)
).dependsOn(vaquita)
