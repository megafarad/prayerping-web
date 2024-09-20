package org.prayerping.services

import org.prayerping.models.{ Page, PrayerRequest }

import java.util.UUID
import scala.concurrent.Future

trait PrayerFeedService {

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
   * Gets a paginated list of prayer requests by their group
   *
   * @param groupId The ID of the group
   * @param page The page of the output
   * @param pageSize The page size in the output
   * @return A future of a page of prayer requests for the group
   */
  def getGroupPrayerRequests(groupId: UUID, page: Int, pageSize: Int,
    requesterId: Option[UUID]): Future[Page[PrayerRequest]]

}
