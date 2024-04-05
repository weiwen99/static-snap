package simple

import java.nio.file.{Files, Path}

import scala.jdk.CollectionConverters.*

class FileService(root: Path) {

  def listDir(path: Path): List[FileMeta] = {
    Files
      .list(path)
      .toList()
      .asScala
      .toList
      .map { d => FileMeta(root = root, path = d) }
  }

  def listDir(path: Path, sortBy: SortBy): List[FileMeta] = {
    val files = listDir(path)
    val rs1   = sortBy.column match
      case SortColumn.Name         => files.sortBy(_.name)
      case SortColumn.Size         => files.sortBy(_.size)
      case SortColumn.Type         => files.sortBy(_.`type`)
      case SortColumn.LastModified => files.sortBy(_.lastModifiedTime)
      case SortColumn.LastAccess   => files.sortBy(_.lastAccessTime)
      case SortColumn.Creation     => files.sortBy(_.creationTime)
    val rs2   = sortBy.order match
      case SortOrder.Asc  => rs1
      case SortOrder.Desc => rs1.reverse
    rs2
  }
}
