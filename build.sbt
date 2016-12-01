name := """test"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "org.sorm-framework" % "sorm" % "0.3.20",
  "org.webjars" % "webjars-play_2.11" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.5",
  "net.kaliber" %% "play-s3" % "8.0.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1"
)

libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"

dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator

offline:= true
