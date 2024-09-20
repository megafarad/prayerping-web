package org.prayerping.services

import org.prayerping.models.{ Page, PrayerRequest, Visibility }

import java.util.UUID
import scala.concurrent.Future

trait PrayerRequestService {

  /**
   * Gets a paginated list of prayer requests, ordered by most recent
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return A future of a [[Page]] of [[PrayerRequest]]s
   */
  def getPrayerRequests(page: Int, pageSize: Int, requesterId: Option[UUID]): Future[Page[PrayerRequest]]

  /**
   * Gets a user's prayer requests
   *
   * @param handle    The handle of the user
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return  A future of a page of prayer requests of a user
   */
  def getUserPrayerRequests(handle: String, domain: Option[String], page: Int, pageSize: Int, includeAnonymous: Boolean,
    requester: Option[UUID]): Future[Page[PrayerRequest]]

  /**
   * Gets a specific prayer request
   * @param id  The ID of the request to get
   * @return A future of an option of a [[PrayerRequest]]
   */
  def getPrayerRequest(id: UUID, requesterId: Option[UUID]): Future[Option[PrayerRequest]]

  /**
   * Creates a new prayer request
   *
   * @param userId    The ID of the requesting user
   * @param request   The text of the request
   * @return  A future of the new prayer request
   */
  def createPrayerRequest(userId: UUID, request: String, isAnonymous: Boolean, visibility: Visibility.Value): Future[PrayerRequest]

  /**
   * Updates an existing prayer request
   *
   * @param id        The ID of the prayer request
   * @param request   The updated text of the request
   * @return          A future of the updated prayer request
   */
  def updatePrayerRequest(id: UUID, request: String, isAnonymous: Boolean, visibility: Visibility.Value, requesterId: Option[UUID]): Future[PrayerRequest]

  /**
   * Deletes a prayer request
   *
   * @param prayerRequest The request to delete
   * @return A future that completes once the prayer request has been deleted
   */
  def deletePrayerRequest(prayerRequest: PrayerRequest): Future[Unit]

}
