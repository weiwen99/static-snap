package simple

import cats.effect._
import cats.effect.kernel.Async
import com.comcast.ip4s.{Host, Port}
import fs2.io.file.Files
import fs2.io.net.Network
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}

object SnapServer {

  def build[F[_]: Async: Files: Network](routes: HttpRoutes[F], host: Host, port: Port): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHttpApp(Router("/" -> routes).orNotFound)
      .withHost(host)
      .withPort(port)
      .withHttp2
      .build
}
