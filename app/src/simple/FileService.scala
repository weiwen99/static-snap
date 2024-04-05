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
}
