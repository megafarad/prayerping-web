package org.prayerping.models.daos

import org.prayerping.models.EntityType
import org.prayerping.models.daos.PostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class HandleDAOImpl @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HandleDAO with DAOSlick {

  /**
   * Gets the raw record for a handle
   *
   * @param handle The handle to search for
   * @return The raw record for the handle if it exists
   */
  def getRawHandle(handle: String, domain: Option[String]): Future[Option[(String, Option[String], EntityType.Value, UUID, Instant)]] =
    db.run(handleTableQuery.filter(_.handle === handle).result.headOption)

  /**
   * Gets suggestions for matching handles
   *
   * @param queryString The string to search handles for
   * @return A seq of matching handle strings
   */
  def getHandleSuggestions(queryString: String): Future[Seq[String]] =
    db.run(handleTableQuery
      .filter(handle => handle.handle like s"%$queryString%")
      .take(10)
      .map(_.handle)
      .result)
}
