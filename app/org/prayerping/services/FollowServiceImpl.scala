package org.prayerping.services

import org.prayerping.models.daos.FollowDAO
import org.prayerping.models.{ Follow, Page }

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class FollowServiceImpl @Inject() (followDAO: FollowDAO) extends FollowService {

  /**
   * Gets a user's follows
   *
   * @param userId   The user ID of the follow
   * @param page     The page of the output
   * @param pageSize The page size in the output
   * @return A future of a page of matching Follow objects
   */
  def getFollows(userId: UUID, page: Int, pageSize: Int): Future[Page[Follow]] =
    followDAO.getFollows(userId, page, pageSize)

  /**
   * Gets a user's followers
   *
   * @param targetUserId The user ID of the follow's target
   * @param page         The page of the output
   * @param pageSize     The page size in the output
   * @return A future of a page of matching Follow objects
   */
  def getFollowers(targetUserId: UUID, page: Int, pageSize: Int): Future[Page[Follow]] =
    followDAO.getFollowers(targetUserId, page, pageSize)

  /**
   * Follows a user
   *
   * @param userId       The user ID of the follower
   * @param targetUserId The ID of the user being followed
   * @return A future of the resulting follow object
   */
  def followUser(userId: UUID, targetUserId: UUID): Future[Follow] =
    followDAO.followUser(userId, targetUserId)

  /**
   * Unfollows a user
   *
   * @param userId       The user ID of the current follower
   * @param targetUserId The ID of the user currently being followed
   * @return
   */
  def unfollowUser(userId: UUID, targetUserId: UUID): Future[Unit] =
    followDAO.unfollowUser(userId, targetUserId)

  /**
   * Gets a follow relationship if it exists.
   *
   * @param userId       The user ID of the follower
   * @param targetUserId The ID of the user being followed
   * @return A future of an option representing a relationship if it exists.
   */
  def getFollow(userId: UUID, targetUserId: UUID): Future[Option[Follow]] =
    followDAO.getFollow(userId, targetUserId)

}
