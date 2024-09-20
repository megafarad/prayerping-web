package org.prayerping.models.daos

import org.prayerping.models.{ Page, PrayerResponse }

import java.util.UUID
import scala.concurrent.Future

trait PrayerResponseDAO {

  /**
   * Gets responses for a prayer request
   *
   * @param requestId The prayer request ID
   * @param page The page of the output
   * @param pageSize The page size in the output
   * @return A future of a paginated list of prayer responses
   */
  def getPrayerResponses(requestId: UUID, page: Int, pageSize: Int, requesterId: Option[UUID]): Future[Page[PrayerResponse]]

  /**
   * Performs a full-text searches for prayer responses of a particular prayer request
   *
   * @param requestId The prayer request ID
   * @param query     The search string
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return          A future of a paginated list of matching prayer responses
   */
  def searchPrayerResponses(requestId: UUID, query: String, page: Int, pageSize: Int, requesterId: Option[UUID]): Future[Page[PrayerResponse]]

  /**
   * Gets a specific prayer response
   *
   * @param responseId The ID of the prayer response
   * @return A future of an option of a prayer response
   */
  def getPrayerResponse(responseId: UUID, requesterId: Option[UUID]): Future[Option[PrayerResponse]]

  /**
   * Creates a prayer response
   *
   * @param requestId   The ID of the prayer request
   * @param userId      The user ID of the responder
   * @param response    The text of the response
   * @return  A future of the created response
   */
  def createPrayerResponse(requestId: UUID, userId: UUID, response: String): Future[PrayerResponse]

  /**
   * Updates a prayer response
   *
   * @param responseId  The ID of the prayer response
   * @param response    The updated text of the response
   * @return    A future of the updated response
   */
  def updatePrayerResponse(responseId: UUID, response: String, requesterId: Option[UUID]): Future[PrayerResponse]
  /**
   * Deletes a prayer response
   *
   * @param responseId The ID of the prayer response to delete
   * @return A future that completes once the prayer response has been deleted
   */
  def deletePrayerResponse(responseId: UUID): Future[Unit]
}
