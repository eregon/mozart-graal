name := "bootcompiler"

version := "2.0-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-deprecation", "-optimize")

libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.3.7"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

EclipseKeys.withSource := true
