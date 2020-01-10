lazy val kunou = (project in file("."))
  .enablePlugins(UniversalPlugin)
  .settings(
      organization := "io.github.samophis",
      name := "Kunou",
      scalaVersion := "2.13.1",
      version := "0.1.0",
      resolvers += Resolver.jcenterRepo,
      resolvers += "jitpack" at "https://jitpack.io",

      libraryDependencies ++= Seq(
        "com.mewna" % "catnip" % "1b5e2a7",
        "io.sentry" % "sentry-logback" % "1.7.29",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
      )
  )