package simple

import java.nio.file.Paths

import scala.util.Random

import cats.effect.*
import cats.effect.unsafe.IORuntime
import cats.implicits.*
import com.typesafe.scalalogging.StrictLogging
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.typelevel.ci.CIString

class SnapRouterSpec extends AnyWordSpec with should.Matchers with BeforeAndAfterAll with StrictLogging {

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  private val dir   = "__static_snap_" + Random.alphanumeric.take(10).mkString
  private val dir1  = s"/tmp/$dir"
  private val dir2  = s"$dir1/subdir"
  private val text1 = "text 1"
  private val file1 = s"$dir1/1.txt"
  private val text2 = """{"key":"value"}"""
  private val file2 = s"$dir2/2.json"

  override def beforeAll(): Unit = {
    os.write(os.Path(file1), text1, createFolders = true)
    os.write(os.Path(file2), text2, createFolders = true)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    os.remove.all(os.Path(dir1))
    super.afterAll()
  }

  "Meta API" should {
    "work" in {
      val router = SnapRouter[IO](Paths.get("/tmp"), "_")
      val actual = router.routes.orNotFound.run(Request[IO](GET, uri"/_/status")).unsafeRunSync()
      actual.status shouldBe Ok
      actual.as[String].unsafeRunSync() shouldBe "simple static server is running.\n"
    }
  }

  "Single File API" should {
    val router = SnapRouter[IO](Paths.get(dir1), "_")
    "serve top level files" in {
      val actual = router.routes.orNotFound.run(Request[IO](GET, uri"/1.txt")).unsafeRunSync()
      actual.status shouldBe Ok
      actual.as[String].unsafeRunSync() shouldBe text1
    }
    "serve nested dir files" in {
      val actual = router.routes.orNotFound.run(Request[IO](GET, uri"/subdir/2.json")).unsafeRunSync()
      actual.status shouldBe Ok
      actual.headers.get(CIString("content-type")).map(_.head.value) shouldBe "application/json".some
      actual.as[String].unsafeRunSync() shouldBe text2
    }
  }

}
