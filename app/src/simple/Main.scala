package simple

import java.nio.file.Path

import cats.effect.{ExitCode, IO}
import cats.implicits.*
import com.comcast.ip4s.{Host, Port}
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import com.typesafe.scalalogging.StrictLogging

object Main
    extends CommandIOApp(name = "static-snap", header = "A Simple Static Server Powered By Scala", version = "0.0.1-SNAPSHOT")
    with StrictLogging {

  import CmdOpts.given

  val rootOpts: Opts[Path] = Opts.argument[Path]("root directory")
  val hostOpts: Opts[Host] = Opts.option[Host]("host", short = "h", help = "host").withDefault(Host.fromString("0.0.0.0").get)
  val portOpts: Opts[Port] = Opts.option[Port]("port", short = "p", help = "port").withDefault(Port.fromInt(8888).get)

  override def main: Opts[IO[ExitCode]] = {
    (hostOpts, portOpts, rootOpts).mapN { (host, port, root) =>
      val routes                 = SnapRouter[IO]().routes
      val runServer: IO[Unit] = SnapServer.build(routes, host, port).use(_ => IO.never)
      for {
        _ <- IO.delay(logger.info("server parameters: root = {}, host = {}, port = {}", root, host, port))
        _ <- runServer
      } yield ExitCode.Success
    }
  }
}
