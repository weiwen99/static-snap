import $ivy.`io.github.hoangmaihuy::mill-universal-packager::0.1.2`
import io.github.hoangmaihuy.mill.packager.archetypes.JavaAppPackagingModule
import mill._
import mill.scalalib.scalafmt.ScalafmtModule
import scalalib._

object Versions {
  val http4sV         = "0.23.26"
  val declineV        = "2.4.1"
  val scalatagsV      = "0.12.0"
  val scalaloggingV   = "3.9.5"
  val logbackClassicV = "1.4.14"
  val scalatestV      = "3.2.18"
  val oslibV          = "0.9.3"
}

object app extends ScalaModule with ScalafmtModule with JavaAppPackagingModule {

  import Versions._

  def scalaVersion = "3.4.0"

  // Define the main class
  def mainClass = Some("simple.Main")

  // Define the top-level directory name for the archived package
  def topLevelDirectory = Some(packageName())

  // Define the version of the package
  def packageVersion = "0.0.1-SNAPSHOT"

  def scalacOptions = Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Wunused:all"
  )

  def ivyDeps = Agg(
    ivy"org.http4s::http4s-ember-server:$http4sV",
    ivy"org.http4s::http4s-dsl:$http4sV",
    ivy"com.lihaoyi::scalatags:$scalatagsV",
    ivy"com.monovore::decline:$declineV",
    ivy"com.monovore::decline-effect:$declineV",
    ivy"com.typesafe.scala-logging::scala-logging:$scalaloggingV",
    ivy"ch.qos.logback:logback-classic:$logbackClassicV"
  )

  object test extends ScalaTests with TestModule.ScalaTest {

    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:$scalatestV",
      ivy"com.lihaoyi::os-lib:$oslibV"
    )
  }
}
