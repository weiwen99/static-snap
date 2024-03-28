package simple

import java.nio.charset.StandardCharsets
import java.nio.file.{Path => NioPath, Paths}

import cats.effect.kernel.Async
import cats.implicits.*
import com.typesafe.scalalogging.StrictLogging
import fs2.io.file.{Files, Path => Fs2Path}
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Accept-Ranges`

/** @param root
  *   提供服务的文件系统根目录
  * @param metaPrefix
  *   元信息 API 的前缀. 比如, 如果 metaPrefix 为 "__", 则元信息 API 为 `/__/status`
  */
class SnapRouter[F[_]: Async: Files](root: NioPath, metaPrefix: String) extends Http4sDsl[F] with StrictLogging {

  require("metaPrefix".nonEmpty, "metaPrefix should not be empty")

  private val META_PREFIX: String = metaPrefix

  // 元信息 API
  private val statusR = HttpRoutes.of[F] { case request @ GET -> Root / META_PREFIX / "status" =>
    Ok("simple static server is running.\n")
  }

  // 单文件 API
  private val fileR = HttpRoutes.of[F] { case request @ GET -> path =>
    // 由于 path 被 URL 编码，所以需要 URL 解码
    val decoded          = java.net.URLDecoder.decode(path.toString, StandardCharsets.UTF_8)
    // 将解码后的路径拼接到根路径下
    val nioPath: NioPath = Paths.get(root.toAbsolutePath.toString, decoded)
    val fs2Path: Fs2Path = Fs2Path.fromNioPath(nioPath)
    StaticFile
      .fromPath(fs2Path, request.some)
      // 支持 Range 请求, 否则播放音视频无法任意快进
      .map(_.putHeaders(`Accept-Ranges`.bytes))
      // 如果文件不存在，返回 404 Not Found
      .getOrElseF({
        logger.error("resource {} not found", nioPath)
        NotFound()
      })
  }

  val routes: HttpRoutes[F] = statusR <+> fileR

}
