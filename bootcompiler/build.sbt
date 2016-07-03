name := "bootcompiler"

version := "2.0-SNAPSHOT"

scalaVersion := "2.11.2"

scalacOptions ++= Seq("-deprecation", "-optimize")

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2"

libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.3.7"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

EclipseKeys.withSource := true

// Work around a bug that prevents generating documentation
unmanagedClasspath in Compile +=
    Attributed.blank(new java.io.File("doesnotexist"))
