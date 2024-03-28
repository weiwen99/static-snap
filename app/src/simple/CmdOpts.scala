package simple

import java.nio.file.{Path, Paths}

import scala.util.*

import cats.data.Validated
import com.comcast.ip4s.{Host, Port}
import com.monovore.decline.Argument

object CmdOpts {

  given Argument[Port] = new Argument[Port] {

    def read(in: String) = {
      in.toIntOption.flatMap(Port.fromInt) match
        case Some(port) => Validated.valid(port)
        case None       => Validated.invalidNel(s"Invalid port: $in")
    }
    def defaultMetavar   = "8888"
  }

  given Argument[Host] = new Argument[Host] {

    def read(in: String) = Host.fromString(in) match
      case Some(host) => Validated.valid(host)
      case None       => Validated.invalidNel(s"Invalid host: $in")

    def defaultMetavar: String = "127.0.0.1"
  }

  given Argument[Path] = new Argument[Path] {

     // 转化为 realpath 以检验路径是否存在
    def read(in: String) = Try(Paths.get(in).toRealPath()) match
      case Success(path)      => Validated.valid(path)
      case Failure(exception) => Validated.invalidNel(s"Invalid path: $in, exception: $exception")

    def defaultMetavar: String = "."
  }
}
