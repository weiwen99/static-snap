package simple

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class QuerySpec extends AnyWordSpec with should.Matchers {

  "SortBy" should {
    "parse valid string" in {
      SortBy.parseString("Name:Asc") shouldBe Right(SortBy(SortColumn.Name, SortOrder.Asc))
      SortBy.parseString("Name:Desc") shouldBe Right(SortBy(SortColumn.Name, SortOrder.Desc))
      SortBy.parseString("LastModified:Desc") shouldBe Right(SortBy(SortColumn.LastModified, SortOrder.Desc))
    }
    "parse invalid string" in {
      SortBy.parseString("Name:Invalid") shouldBe Left("enum simple.SortOrder has no case with name: Invalid")
      SortBy.parseString("Name:Invalid:Ext") shouldBe Left("Invalid SortBy format")
    }
  }
}
