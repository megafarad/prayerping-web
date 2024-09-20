package org.prayerping.models.daos

import org.prayerping.models.EntityType

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

trait HandleDAO {

  /**
   * Gets the raw record for a handle
   *
   * @param handle  The handle to search for
   * @return        The raw record for the handle if it exists
   */
  def getRawHandle(handle: String, domain: Option[String]): Future[Option[(String, Option[String], EntityType.Value, UUID, Instant)]]

  /**
   * Gets suggestions for matching handles
   *
   * @param queryString The string to search handles for
   * @return  A seq of matching handle strings
   */
  def getHandleSuggestions(queryString: String): Future[Seq[String]]
}
