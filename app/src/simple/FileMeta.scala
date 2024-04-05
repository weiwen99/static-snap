package simple

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.nio.file.attribute.{BasicFileAttributes, FileTime}
import java.time.{ZonedDateTime, ZoneId}
import java.time.format.DateTimeFormatter

/** 文件元信息
  *
  * @param root
  *   根目录
  * @param path
  *   文件路径
  */
final case class FileMeta(root: Path, path: Path) {

  import FileMeta.*

  // 目录名或者文件名
  val name: String = path.getParent().relativize(path).toString

  // 文件属性集
  val attrs: BasicFileAttributes = Files.readAttributes(path, classOf[BasicFileAttributes])

  // 是否是目录
  val isDirectory: Boolean = attrs.isDirectory()

  // 文件最后访问时间
  val lastAccessTime: ZonedDateTime = attrs.lastAccessTime().toZonedDateTime

  // 文件最后修改时间
  val lastModifiedTime: ZonedDateTime = attrs.lastModifiedTime().toZonedDateTime

  // 文件创建时间
  val creationTime: ZonedDateTime = attrs.creationTime().toZonedDateTime
  // 相对于根目录的路径
  val relativeToRoot: Path        = root.relativize(path)

  // 文件大小，单位 Byte
  val size = attrs.size()

  // 人类可读的文件大小
  val humanSize: String = size match {
    case s if s < 1024L                 => s"$s B"
    case s if s < 1024L * 1024L         => s"${s / 1024L} KB"
    case s if s < 1024L * 1024L * 1024L => s"${s / 1024L / 1024L} MB"
    case s                              => s"${s / 1024L / 1024L / 1024L} GB"
  }

  // 文件类型
  val `type`: String = if (isDirectory) "Directory" else "File"

  // URL 编码后的路径，用于超链接. 将路径中的每个部分进行 URL 编码，避免 x/y/z -> x%2Fy%2Fz
  val href: String =
    "/" + relativeToRoot.toString.split("/").map(URLEncoder.encode(_, StandardCharsets.UTF_8)).mkString("/")
}

object FileMeta {

  private val zoneId: ZoneId = ZoneId.systemDefault()

  private[FileMeta] val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  // 将 FileTime 转换为 ZonedDateTime
  extension (fileTime: FileTime) {
    def toZonedDateTime: ZonedDateTime = fileTime.toInstant().atZone(zoneId)
  }

  // 生成适合显示的时间日期
  extension (zonedDateTime: ZonedDateTime) {
    def formatted: String = formatter.format(zonedDateTime)
  }
}
