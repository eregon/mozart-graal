name := "bootcompiler"

version := "2.0-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-deprecation", "-optimize")

libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.3.7"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

EclipseKeys.withSource := true

// Work around a bug that prevents generating documentation
unmanagedClasspath in Compile +=
    Attributed.blank(new java.io.File("doesnotexist"))

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "truffle", "instrument") => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
