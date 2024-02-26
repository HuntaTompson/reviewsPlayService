name := "reviewsPlayService"

version := "1.0"

lazy val `reviewsplayservice` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(jdbc, ehcache, ws, specs2 % Test, guice, "org.jsoup" % "jsoup" % "1.15.4")

import com.typesafe.sbt.packager.docker._
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown
dockerBaseImage := "openjdk:17-jdk-slim"
enablePlugins(DockerPlugin)
