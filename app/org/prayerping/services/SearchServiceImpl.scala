package org.prayerping.services

import org.prayerping.models.daos.{ PrayerGroupDAO, PrayerRequestDAO, PrayerResponseDAO }
import org.prayerping.models.{ Page, PrayerGroupProfile, PrayerRequest, PrayerResponse }

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class SearchServiceImpl @Inject() (
  prayerRequestDAO: PrayerRequestDAO,
  prayerResponseDAO: PrayerResponseDAO,
  prayerGroupDAO: PrayerGroupDAO) extends SearchService {
  /**
   * Performs a full-text search of prayer requests
   *
   * @param query    The search string
   * @param page     The page of the search results
   * @param pageSize The page size in the search results
   * @return A future of a page of search results
   */
  def searchPrayerRequests(query: String, page: Int, pageSize: Int, requesterId: Option[UUID]): Future[Page[PrayerRequest]] =
    prayerRequestDAO.searchPrayerRequests(query, page, pageSize, requesterId)

  /**
   * Performs a full-text search of a user's prayer requests
   *
   * @param handle   The handle of the user
   * @param query    The search string
   * @param page     The page of the search results
   * @param pageSize The page size in the search results
   * @return A future of a page of search results
   */
  def searchUserPrayerRequests(handle: String, domain: Option[String], query: String, page: Int, pageSize: Int,
    includeAnonymous: Boolean, requesterId: Option[UUID]): Future[Page[PrayerRequest]] =
    prayerRequestDAO.searchUserPrayerRequests(handle, None, query, page, pageSize, includeAnonymous, requesterId)

  /**
   * Performs a full-text search of prayer requests in a specific group
   *
   * @param groupId  The ID of the group to search
   * @param query    The search query string
   * @param page     The page of the search results
   * @param pageSize The page size in the search results
   * @return A future of a page of search results
   */
  def searchGroupPrayerRequests(groupId: UUID, query: String, page: Int, pageSize: Int,
    requesterId: Option[UUID]): Future[Page[PrayerRequest]] =
    prayerRequestDAO.searchGroupPrayerRequests(groupId, query, page, pageSize, requesterId)

  /**
   * Performs a full-text searches for prayer responses of a particular prayer request
   *
   * @param requestId The prayer request ID
   * @param query     The search string
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return A future of a paginated list of matching prayer responses
   */
  def searchPrayerResponses(requestId: UUID, query: String, page: Int, pageSize: Int,
    requesterId: Option[UUID]): Future[Page[PrayerResponse]] =
    prayerResponseDAO.searchPrayerResponses(requestId, query, page, pageSize, requesterId)

  /**
   * Performs a full-text search of prayer groups
   *
   * @param query    The search string
   * @param page     The page of the search results
   * @param pageSize The page size in the search results
   * @return A future of a page of search results
   */
  def searchPrayerGroups(query: String, page: Int, pageSize: Int): Future[Page[PrayerGroupProfile]] =
    prayerGroupDAO.searchPrayerGroups(query, page, pageSize)

}
