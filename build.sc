import $ivy.`io.github.hoangmaihuy::mill-universal-packager::0.1.2`
import io.github.hoangmaihuy.mill.packager.archetypes.JavaAppPackagingModule
import mill._
import mill.scalalib.scalafmt.ScalafmtModule
import scalalib._

object Versions {
  val scalaV          = "3.4.1"
  val http4sV         = "0.23.26"
  val declineV        = "2.4.1"
  val scalatagsV      = "0.12.0"
  val scalaloggingV   = "3.9.5"
  val logbackClassicV = "1.4.14"
  val scalatestV      = "3.2.18"
  val oslibV          = "0.9.3"
}

// add GraalVM native-image support, use `mill {subproject}.graalvmNativeImage` to generate native image
trait GraalvmNativeImageModule extends JavaModule {

  // GraalVM native-image executable in the system
  def graalvmNativeExecutable: T[String] = "native-image"

  // GraalVM native-image main class
  def graalvmNativeMainClass: T[String]

  // GraalVM native-image options
  def graalvmNativeImageOptions: T[Seq[String]] = T(Seq.empty[String])

  // GraalVM native-image executable script name
  def graalvmExecutableScriptName: T[String]

  // GraalVM native-image class paths
  def graalvmNativeClassPaths: T[Seq[os.Path]] = T { runClasspath().map(_.path) }

  // GraalVM native-image task
  def graalvmNativeImage: T[Unit] = T {
    val out   = T.dest / graalvmExecutableScriptName()
    val cmds1 = graalvmNativeExecutable() :: "--class-path" :: graalvmNativeClassPaths().mkString(":") :: Nil
    val cmds2 = "-o" :: out.toString :: graalvmNativeMainClass() :: Nil
    val cmds  = cmds1 ++ graalvmNativeImageOptions() ++ cmds2
    graalvmNativeImageOptions() ++
      List(
        "-o",
        out.toString,
        graalvmNativeMainClass()
      )
    T.log.info(s"""executing: ${cmds.mkString(" ")}""")
    val r     = os.proc(cmds).call()
    if (r.exitCode == 0) {
      T.log.info(s"generated GraalVM native image: $out")
      val size     = os.size(out)
      val sizeInKB = size / 1024.0
      val sizeInMB = sizeInKB / 1024.0
      T.log.info(s"""generated GraalVM native image size: $size Bytes ≈ ${"%.3f".format(sizeInKB)} KB ≈ ${"%.3f".format(sizeInMB)} MB""")
    } else {
      T.log.error("got non ZERO cmd result when call native-image")
    }
  }
}

object app extends ScalaModule with ScalafmtModule with JavaAppPackagingModule with GraalvmNativeImageModule {

  import Versions._

  def scalaVersion = scalaV

  def repositoriesTask = T.task {
    Seq(coursier.maven.MavenRepository("http://maven.aliyun.com/")) ++ super.repositoriesTask()
  }

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

  def graalvmNativeMainClass: T[String] = T("simple.Main")

  def graalvmNativeImageOptions: T[Seq[String]] = T(
    Seq(
      "--verbose",
      "--static",
      "--initialize-at-build-time",
      "--no-fallback",
      // "--libc=musl",
      "-H:+UnlockExperimentalVMOptions",
      "-H:IncludeResources=.*/(.*.css)|(.*.js)$",
      "-H:-UnlockExperimentalVMOptions"
    )
  )

  // Simple Scala Static Snap Server: s5
  def graalvmExecutableScriptName: T[String] = T("s5")
}
