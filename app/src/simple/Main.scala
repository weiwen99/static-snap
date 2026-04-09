package simple

import java.nio.file.Path
import java.util.concurrent.CountDownLatch

import scala.concurrent.Await
import scala.concurrent.duration.*

import cats.effect.IO
import cats.implicits.*
import com.comcast.ip4s.{Host, Port}
import com.monovore.decline.{Command, Opts}
import com.typesafe.scalalogging.StrictLogging

object Main extends StrictLogging {

  import CmdOpts.given

  private val rootOpts: Opts[Path] = Opts.argument[Path]("root directory")
  private val hostOpts: Opts[Host] = Opts.option[Host]("host", short = "h", help = "host").withDefault(Host.fromString("0.0.0.0").get)
  private val portOpts: Opts[Port] = Opts.option[Port]("port", short = "p", help = "port").withDefault(Port.fromInt(8888).get)

  private val command: Command[(Host, Port, Path)] =
    Command("static-snap", "A Simple Static Server Powered By Scala")((hostOpts, portOpts, rootOpts).tupled)

  def main(args: Array[String]): Unit = {
    import cats.effect.unsafe.implicits.global

    val latch = new CountDownLatch(1)

    val program: IO[Unit] = command.parse(args.toList) match {
      case Left(help) =>
        IO.delay(System.err.println(help))

      case Right((host, port, root)) =>
        val routes = SnapRouter[IO](root, "__").routes
        IO.delay(logger.info("server parameters: root = {}, host = {}, port = {}", root, host, port)) *>
          SnapServer
            .build(routes, host, port)
            .useForever
            .guarantee(IO.delay(logger.info("server stopped.")))
    }

    val cancel: () => scala.concurrent.Future[Unit] = program.unsafeRunCancelable()

    sys.addShutdownHook {
      Await.result(cancel(), 10.seconds)
      latch.countDown()
    }

    latch.await()
  }
}
