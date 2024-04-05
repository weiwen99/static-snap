package simple

import scala.util.Try

import cats.implicits.*

enum SortOrder:
  case Asc  extends SortOrder
  case Desc extends SortOrder

enum SortColumn:
  case Name         extends SortColumn
  case Size         extends SortColumn
  case Type         extends SortColumn
  case LastModified extends SortColumn
  case LastAccess   extends SortColumn
  case Creation     extends SortColumn

final case class SortBy(column: SortColumn, order: SortOrder)

object SortBy {

  def parseString(s: String): Either[String, SortBy] = {
    s.split(":").toList match {
      case column :: order :: Nil =>
        val r = for {
          column <- Try(SortColumn.valueOf(column)).toEither.leftMap(_.getMessage())
          order  <- Try(SortOrder.valueOf(order)).toEither.leftMap(_.getMessage())
        } yield SortBy(column, order)
        r
      case _                      => Left("Invalid SortBy format")
    }
  }
}
