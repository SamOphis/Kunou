val runKunou = taskKey[Unit]("Runs Kunou locally.")

lazy val kunou = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    organization := "io.github.samophis",
    name := "Kunou",
    scalaVersion := "2.13.1",
    version := "0.1.0",
    resolvers += Resolver.jcenterRepo,
    resolvers += "jitpack" at "https://jitpack.io",

      libraryDependencies ++= Seq(
        // Catnip v2
        "com.mewna" % "catnip" % "93cfcdb",

        // Logging Stuff
        "io.sentry" % "sentry-logback" % "1.7.29",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

        // ClassGraph (used to automatically register command classes)
        "io.github.classgraph" % "classgraph" % "4.8.60",

        // Database Stuff (for now we're sticking with Redis)
        "net.debasishg" %% "redisclient" % "3.20",

        // Weeb4J Client
        "com.github.natanbc" % "weeb4j" % "3.5"
      ),

    runKunou := {
      import sys.process._

      (Universal / packageXzTarball).value

      val stdout = Process("./run.sh").lineStream.iterator
      while (true) {
        if (stdout.hasNext)
          println(stdout.next)
      }
    }
  )

