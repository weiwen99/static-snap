package simple

import cats.effect.*
import cats.effect.unsafe.IORuntime
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.uri
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class SnapRouterSpec extends AnyWordSpec with should.Matchers {

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  "API `/_status`" should {
    "work" in {
      val router = SnapRouter[IO]()
      val actual = router.routes.orNotFound.run(Request[IO](GET, uri"/_status")).unsafeRunSync()
      actual.status shouldBe Ok
      actual.as[String].unsafeRunSync() shouldBe "simple static server is running.\n"
    }
  }
}
