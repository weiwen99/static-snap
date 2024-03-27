package simple

import cats.effect.{ExitCode, IO}
import cats.implicits.*
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import com.typesafe.scalalogging.StrictLogging

object Main
    extends CommandIOApp(name = "static-snap", header = "A Simple Static Server Powered By Scala", version = "0.0.1-SNAPSHOT")
    with StrictLogging {

  val rootOpts: Opts[String] = Opts.argument[String]("root directory")
  val hostOpts: Opts[String] = Opts.option[String]("host", short = "h", help = "host").withDefault("0.0.0.0")
  val portOpts: Opts[Int]    = Opts.option[Int]("port", short = "p", help = "port").withDefault(8888)

  override def main: Opts[IO[ExitCode]] = {
    (hostOpts, portOpts, rootOpts).mapN { (host, port, root) =>
      for {
        _ <- IO.delay(logger.info(s"hello world!"))
        _ <- IO.delay(logger.info("got parameters: host = {}, port = {}, root = {}", host, port, root))
      } yield ExitCode.Success
    }
  }
}
