package org.prayerping.models.daos

import org.prayerping.models.{PrayerGroup, PrayerGroupProfile}
import org.prayerping.modules.JobModule
import org.prayerping.test.util.CommonTest
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class PrayerGroupDAOTest extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterEach
  with CommonTest {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("slick.dbs.default.profile" -> "slick.jdbc.PostgresProfile$")
    .configure("slick.dbs.default.db.driver" -> "org.postgresql.Driver")
    .configure("slick.dbs.default.db.url" -> "jdbc:postgresql://localhost:5432/prayerpingtest")
    .configure("slick.dbs.default.db.user" -> "postgres")
    .configure("slick.dbs.default.db.password" -> "postgres")
    .disable[JobModule]
    .build()

  val userDAO: UserDAO = app.injector.instanceOf[UserDAO]
  val prayerGroupDAO: PrayerGroupDAO = app.injector.instanceOf[PrayerGroupDAO]

  val databaseApi: DBApi = app.injector.instanceOf[DBApi]

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(1.second))

  def setup: Future[PrayerGroupProfile] = for {
    _ <- userDAO.save(testUser)
    createdPrayerGroup <- prayerGroupDAO.createPrayerGroup(
      "Test group",
      "TestGroup",
      None,
      "Test group",
      testUser.userID,
      "Fake Public Key",
      None,
      None
    )
  } yield createdPrayerGroup

  "PrayerGroupDAO" should {
    "Upsert and get properly" in {
      setup.futureValue
    }

    "Search properly" in {
      whenReady(setup) {
        _ => whenReady(prayerGroupDAO.searchPrayerGroups("test", 0, 10)) {
          searchResults => searchResults.items.size must be(1)
        }
      }
    }
  }

  override def beforeEach(): Unit = {
    Evolutions.applyEvolutions(databaseApi.database("default"))
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
    super.afterEach()
  }

}
