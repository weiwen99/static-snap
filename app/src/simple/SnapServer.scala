package simple

import scala.concurrent.duration.*

import cats.effect.*
import cats.effect.kernel.Async
import com.comcast.ip4s.{Host, Port}
import fs2.io.net.Network
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.server.staticcontent.*

object SnapServer {

  def build[F[_]: Async: Network](routes: HttpRoutes[F], host: Host, port: Port): Resource[F, Server] = {
    val resourceStaticRoutes = ResourceServiceBuilder("/static").withPathPrefix("/static").toRoutes
    val router               = Router("/" -> routes, "__" -> resourceStaticRoutes).orNotFound
    EmberServerBuilder
      .default[F]
      .withHttpApp(router)
      .withHost(host)
      .withPort(port)
      .withHttp2
      .withShutdownTimeout(3.seconds)
      .build
  }
}
