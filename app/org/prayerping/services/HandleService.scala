package org.prayerping.services

import org.prayerping.models.Handle

import scala.concurrent.Future

trait HandleService {

  /**
   * Gets a handle if it exists
   *
   * @param handle The handle to get
   * @return  A handle with the object it points to
   */
  def getHandle(handle: String, domain: Option[String]): Future[Option[Handle]]

  /**
   * Gets suggestions for matching handles
   *
   * @param queryString   The string to search handles for
   * @return      A seq of strings that match the search string
   */
  def getHandleSuggestions(queryString: String): Future[Seq[String]]

}
