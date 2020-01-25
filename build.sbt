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
        "com.mewna" % "catnip" % "0ad521e64addf4261e5836f61080c43b97aa3f60",
        "io.sentry" % "sentry-logback" % "1.7.29",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
        "io.github.classgraph" % "classgraph" % "4.8.60"
      )
  )
