package simple

import java.nio.charset.StandardCharsets
import java.nio.file.{Path => NioPath, Paths}

import cats.effect.kernel.Async
import cats.implicits.*
import com.typesafe.scalalogging.StrictLogging
import fs2.io.file.{Files, Path => Fs2Path}
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.headers.{`Accept-Ranges`, `Content-Type`}
import scalatags.Text.*
import scalatags.Text.all.*

import simple.FileMeta.*

given QueryParamDecoder[SortBy] =
  QueryParamDecoder[String].emap(s => SortBy.parseString(s).leftMap(t => ParseFailure(t, t)))

object OptionalSortByQueryParamMatcher extends OptionalQueryParamDecoderMatcher[SortBy]("sort")

/** @param root
  *   提供服务的文件系统根目录
  * @param metaPrefix
  *   元信息 API 的前缀. 比如, 如果 metaPrefix 为 "__", 则元信息 API 为 `/__/status`
  */
class SnapRouter[F[_]: Async: Files](root: NioPath, metaPrefix: String) extends Http4sDsl[F] with StrictLogging {

  private val DEFAULT_SORT_BY: SortBy = SortBy(SortColumn.Name, SortOrder.Asc)

  require("metaPrefix".nonEmpty, "metaPrefix should not be empty")

  private val META_PREFIX: String = metaPrefix

  private given EntityEncoder[F, TypedTag[String]] =
    EntityEncoder
      .stringEncoder[F]
      .contramap[TypedTag[String]](content => "<!DOCTYPE html>" + content.render)
      .withContentType(`Content-Type`(MediaType.text.html, Charset.`UTF-8`))

  // 元信息 API
  private val statusR = HttpRoutes.of[F] { case request @ GET -> Root / META_PREFIX / "status" =>
    Ok("simple static server is running.\n")
  }

  // 列出目录内容或者返回文件
  private val mainR = HttpRoutes.of[F] { case request @ GET -> path :? OptionalSortByQueryParamMatcher(sortByOpt) =>
    val sortBy           = sortByOpt.getOrElse(DEFAULT_SORT_BY)
    // 由于 path 被 URL 编码，所以需要 URL 解码
    val decoded          = java.net.URLDecoder.decode(path.toString, StandardCharsets.UTF_8)
    // 将解码后的路径拼接到根路径下
    val nioPath: NioPath = Paths.get(root.toAbsolutePath.toString, decoded)
    nioPath match {
      // 如果文件不存在，返回 404 Not Found
      case n if !java.nio.file.Files.exists(n)       => NotFound()
      // 如果文件不在根目录下，返回 403 Forbidden. (虽然 URL path 理论上能阻止溢出根目录范围)
      case n if !n.toAbsolutePath().startsWith(root) => Forbidden()
      // 如果是目录，列出目录内容
      case n if (java.nio.file.Files.isDirectory(n)) => Ok(listDir(nioPath, sortBy))
      // 如果是文件，返回文件内容
      case n                                         => serveFile(n, request.some)
    }
  }

  private def serveFile(path: NioPath, req: Option[Request[F]]) =
    StaticFile
      .fromPath(Fs2Path.fromNioPath(path), req)
      // 支持 Range 请求, 否则播放音视频无法任意快进
      .map(_.putHeaders(`Accept-Ranges`.bytes))
      // 如果文件不存在，返回 404 Not Found. 这里仅仅是为了形式上的正确，因为前面已经处理了文件不存在的情况
      .getOrElseF(NotFound())

  private def listDir(path: NioPath, sortBy: SortBy): TypedTag[String] = {
    val simpleDirName = s"/${root.relativize(path)}"
    val trs           = FileService(root).listDir(path, sortBy).map { f =>
      tr(
        td(a(href := f.href, f.name)),
        td(f.`type`),
        td(f.humanSize),
        td(f.lastModifiedTime.formatted),
        td(f.lastAccessTime.formatted),
        td(f.creationTime.formatted)
      )
    }
    html(
      lang := "zh-CN",
      head(
        meta(charset := "utf-8"),
        meta(name    := "viewport", content := "width=device-width, initial-scale=1.0, user-scalable=no, maximum-scale=1, minimum-scale=1"),
        link(rel     := "stylesheet", href  := "/__/static/css/materialize.min.css"),
        link(rel     := "stylesheet", href  := "/__/static/font/google-fonts-icon.css"),
        script(src   := "/__/static/js/materialize.min.js"),
        tags2.title(simpleDirName)
      ),
      body(
        style := "padding: 0 1em 0 1em;",
        h6(s"Index of $simpleDirName"),
        table(
          cls := "striped",
          tableHeaderOf(sortBy),
          if (path != root) tr(td(a(href := "../", "../"))) else tr(),
          tbody(trs)
        )
      )
    )
  }

  def tableHeaderOf(sortBy: SortBy): TypedTag[String] = {
    val arrow                    = sortBy.order match
      case SortOrder.Asc  => "▲"
      case SortOrder.Desc => "▼"
    def thOf(column: SortColumn) =
      th(
        a(
          href := s"?sort=$column:${if sortBy.column == column && sortBy.order == SortOrder.Asc then SortOrder.Desc else SortOrder.Asc}",
          column.toString + (if sortBy.column == column then arrow else "")
        )
      )
    tr(
      List(SortColumn.Name, SortColumn.Type, SortColumn.Size, SortColumn.LastModified, SortColumn.LastAccess, SortColumn.Creation).map(thOf)
    )
  }

  val routes: HttpRoutes[F] = statusR <+> mainR

}
