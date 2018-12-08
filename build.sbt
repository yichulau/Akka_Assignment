name := "Assignment22"

version := "0.1"

scalaVersion := "2.12.8"


addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "8.0.181-R13",
  "org.scalafx" %% "scalafxml-core-sfx8" % "0.4",
  "com.typesafe.akka" %% "akka-actor" % "2.5.17",
  "com.typesafe.akka" %% "akka-remote" % "2.5.17",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.17"
)