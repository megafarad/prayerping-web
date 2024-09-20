package org.prayerping.models.daos

import org.prayerping.models.{ Follow, Page }

import java.util.UUID
import scala.concurrent.Future

trait FollowDAO {

  /**
   * Gets a user's follows
   *
   * @param userId    The user ID of the follow
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return  A future of a page of matching Follow objects
   */
  def getFollows(userId: UUID, page: Int, pageSize: Int): Future[Page[Follow]]

  /**
   * Gets a user's followers
   *
   * @param targetUserId The user ID of the follow's target
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return A future of a page of matching Follow objects
   */
  def getFollowers(targetUserId: UUID, page: Int, pageSize: Int): Future[Page[Follow]]

  /**
   * Gets a list of User IDs that follow the target user.
   *
   * @param targetUserId The user ID of the follower's target
   * @return A list of user IDs that follow the target user.
   */
  def getFollowers(targetUserId: UUID): Future[Seq[UUID]]

  /**
   * Follows a user
   *
   * @param userId          The user ID of the follower
   * @param targetUserId    The ID of the user being followed
   * @return  A future of the resulting follow object
   */
  def followUser(userId: UUID, targetUserId: UUID): Future[Follow]

  /**
   * Unfollows a user
   *
   * @param userId          The user ID of the current follower
   * @param targetUserId    The ID of the user currently being followed
   * @return
   */
  def unfollowUser(userId: UUID, targetUserId: UUID): Future[Unit]

  /**
   * Gets a follow relationship if it exists.
   *
   * @param userId        The user ID of the follower
   * @param targetUserId  The ID of the user being followed
   * @return A future of an option representing a relationship if it exists.
   */
  def getFollow(userId: UUID, targetUserId: UUID): Future[Option[Follow]]

}
