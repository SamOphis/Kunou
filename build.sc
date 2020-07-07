import mill._
import scalalib._
import coursier.maven.MavenRepository

object bot extends ScalaModule {
  def scalaVersion = "2.13.1"
  override def ivyDeps = Agg(
    ivy"com.github.Katrix:AckCord:04c091a",
    //ivy"net.katsstuff::ackcord:0.17.0-SNAPSHOT",
    ivy"net.debasishg::redisclient:3.30",
    ivy"io.github.classgraph:classgraph:4.8.87",
    ivy"io.sentry:sentry-logback:1.7.30",
    ivy"com.typesafe.scala-logging::scala-logging:3.9.2"
  )
  override def repositories = super.repositories ++ Seq(
    MavenRepository("https://jitpack.io"),
    MavenRepository("https://jcenter.bintray.com")
  )
}