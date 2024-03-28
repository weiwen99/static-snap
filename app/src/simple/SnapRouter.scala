package simple

import cats.effect.kernel.Async
import fs2.io.file.Files
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class SnapRouter[F[_]: Async: Files]() extends Http4sDsl[F] {

  private val statusR = HttpRoutes.of[F] { case request @ GET -> Root / "_status" =>
    Ok("simple static server is running.\n")
  }

  val routes: HttpRoutes[F] = statusR
}
