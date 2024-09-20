package org.prayerping.models.daos

import org.prayerping.models.{Page, PrayerRequest, PrayerRequestGroup, User, Visibility}

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

trait PrayerRequestDAO {

  /**
   * Gets a paginated list of prayer requests, ordered by most recent
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return A future of a [[Page]] of [[PrayerRequest]]s
   */
  def getPrayerRequests(page: Int, pageSize: Int, requesterId: Option[UUID]): Future[Page[PrayerRequest]]

  /**
   * Gets a feed of followed user's prayer requests as well as group prayer requests
   * the user belongs to.
   *
   * @param userId    The ID of the user
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return    A future of a page of prayer requests
   */
  def getPrayerRequestFeed(userId: UUID, page: Int, pageSize: Int): Future[Page[PrayerRequest]]

  /**
   * Performs a full-text search of prayer requests
   *
   * @param query     The search string
   * @param page      The page of the search results
   * @param pageSize  The page size in the search results
   * @return A future of a page of search results
   */
  def searchPrayerRequests(query: String, page: Int, pageSize: Int, requester: Option[UUID]): Future[Page[PrayerRequest]]

  /**
   * Gets a user's prayer requests
   *
   * @param handle    The handle of the user
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return  A future of a page of prayer requests of a user
   */
  def getUserPrayerRequests(handle: String, domain: Option[String], page: Int, pageSize: Int,
    includeAnonymous: Boolean, requester: Option[UUID]): Future[Page[PrayerRequest]]

  /**
   * Performs a full-text search of a user's prayer requests
   *
   * @param handle    The handle of the user
   * @param query     The search string
   * @param page      The page of the search results
   * @param pageSize  The page size in the search results
   * @return  A future of a page of search results
   */
  def searchUserPrayerRequests(handle: String, domain: Option[String], query: String, page: Int, pageSize: Int,
    includeAnonymous: Boolean, requester: Option[UUID]): Future[Page[PrayerRequest]]

  /**
   * Gets a paginated list of prayer requests by their group
   *
   * @param groupId The ID of the group
   * @param page The page of the output
   * @param pageSize The page size in the output
   * @return A future of a page of prayer requests for the group
   */
  def getGroupPrayerRequests(groupId: UUID, page: Int, pageSize: Int,
    requesterId: Option[UUID]): Future[Page[PrayerRequest]]

  /**
   * Performs a full-text search of prayer requests in a specific group
   *
   * @param groupId   The ID of the group to search
   * @param query     The search query string
   * @param page      The page of the search results
   * @param pageSize  The page size in the search results
   * @return  A future of a page of search results
   */
  def searchGroupPrayerRequests(groupId: UUID, query: String, page: Int, pageSize: Int,
    requester: Option[UUID]): Future[Page[PrayerRequest]]

  /**
   * Shares a prayer request with a group
   *
   * @param requestId   The ID of the prayer request
   * @param groupId     The ID of the prayer group
   * @return A future of an object representing the share
   */
  def sharePrayerRequestWithGroup(requestId: UUID, groupId: UUID): Future[PrayerRequestGroup]

  /**
   * Removes a prayer request from a group
   *
   * @param requestId   The ID of the prayer request
   * @param groupId     The ID of the prayer group
   * @return
   */
  def removePrayerRequestFromGroup(requestId: UUID, groupId: UUID): Future[Unit]

  /**
   * Gets a specific prayer request
   * @param id  The ID of the request to get
   * @return A future of an option of a [[PrayerRequest]]
   */
  def getPrayerRequest(id: UUID, requesterId: Option[UUID]): Future[Option[PrayerRequest]]

  /**
   * Gets existing mentions for a prayer request (used by service for logic).
   *
   * @param prayerRequestId The ID of the prayer request
   * @return A Future of a Set of mentions
   */
  def getMentionsForPrayerRequest(prayerRequestId: UUID): Future[Set[(String, Option[String])]]

  /**
   * Creates a new prayer request
   *
   * @param userId    The ID of the requesting user
   * @param request   The text of the request
   * @return  A future of the new prayer request
   */
  def createNewPrayerRequest(userId: UUID, request: String, isAnonymous: Boolean, visibility: Visibility.Value,
                             mentions: Set[String]): Future[PrayerRequest]

  /**
   * Updates an existing prayer request
   *
   * @param id        The ID of the prayer request
   * @param request   The updated text of the request
   * @return          A future of the updated prayer request
   */
  def updatePrayerRequest(id: UUID, request: String, isAnonymous: Boolean, visibility: Visibility.Value, mentionRowTuplesToAdd: Seq[(UUID, UUID, String, Option[String], Instant)], mentionsToRemove: Set[(String, Option[String])], requesterId: Option[UUID]): Future[PrayerRequest]

  /**
   * Deletes a prayer request
   *
   * @param id The ID of the request to delete
   * @return A future that completes once the prayer request has been deleted
   */
  def deletePrayerRequest(id: UUID): Future[Unit]
}
