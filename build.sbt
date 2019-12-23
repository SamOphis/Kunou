lazy val kunou = (project in file("."))
  .enablePlugins(UniversalPlugin)
  .settings(
    organization := "io.github.samophis",
    name := "Kunou",
    scalaVersion := "2.13.1",
    version := "0.1.0",
    resolvers += Resolver.jcenterRepo,
    libraryDependencies += "net.katsstuff" %% "ackcord" % "0.15.0"
  )