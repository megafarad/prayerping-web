package org.prayerping.models.daos

import org.prayerping.models.{ Follow, Page, User }
import PostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class FollowDAOImpl @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends FollowDAO with DAOSlick {

  /**
   * Gets a user's follows
   *
   * @param userId The user ID of the follow
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return A future of a page of matching Follow objects
   */
  def getFollows(userId: UUID, page: Int, pageSize: Int): Future[Page[Follow]] = {
    val totalQuery = followTableQuery.filter(_.userId === userId)
    getFollows(totalQuery, page, pageSize)
  }

  /**
   * Gets a user's followers
   *
   * @param targetUserId The user ID of the follow's target
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return A future of a page of matching Follow objects
   */
  def getFollowers(targetUserId: UUID, page: Int, pageSize: Int): Future[Page[Follow]] = {
    val totalQuery = followTableQuery.filter(_.targetUserId === targetUserId)
    getFollows(totalQuery, page, pageSize)
  }

  /**
   * Gets a list of User IDs that follow the target user.
   *
   * @param targetUserId The user ID of the follower's target
   * @return A list of user IDs that follow the target user.
   */
  def getFollowers(targetUserId: UUID): Future[Seq[UUID]] = {
    db.run(followTableQuery.filter(_.targetUserId === targetUserId).map(_.userId).result)
  }

  private def getFollows(totalQuery: Query[FollowTable, (UUID, UUID, Instant), Seq], page: Int,
    pageSize: Int): Future[Page[Follow]] = {
    val offset = page * pageSize
    val paginatedQuery = totalQuery.drop(offset).take(pageSize)
    val withUsers = joinUsers(paginatedQuery)
    for {
      paginatedItems <- db.run(withUsers.result) map {
        raw =>
          raw map {
            case (followingUser, targetUser, whenCreated) => Follow(followingUser.getPublicUserProfile, targetUser.getPublicUserProfile, whenCreated)
          }
      }
      totalItems <- db.run(totalQuery.length.result)
    } yield Page(paginatedItems, page, offset, totalItems.toLong)
  }

  private def joinUsers(paginatedQuery: Query[FollowTable, (UUID, UUID, Instant), Seq]) = {
    paginatedQuery.join(userTableQuery)
      .on(_.userId === _.id)
      .join(userTableQuery)
      .on(_._1.targetUserId === _.id)
      .map {
        case ((followTable, followingUserTable), targetUserTable) =>
          (followingUserTable, targetUserTable, followTable.whenCreated)
      }
  }

  /**
   * Follows a user
   *
   * @param userId       The user ID of the follower
   * @param targetUserId The ID of the user being followed
   * @return A future of the resulting follow object
   */
  def followUser(userId: UUID, targetUserId: UUID): Future[Follow] = {
    for {
      _ <- db.run(followTableQuery.insertOrUpdate((userId, targetUserId, Instant.now())))
      follow <- {
        val query = followTableQuery.filter(row => row.userId === userId && row.targetUserId === targetUserId)
        val withUsers = joinUsers(query)
        db.run(withUsers.result.head) map {
          case (followingUser, targetUser, whenCreated) => Follow(followingUser.getPublicUserProfile, targetUser.getPublicUserProfile,
            whenCreated)
        }
      }
    } yield follow
  }

  /**
   * Gets a follow relationship if it exists.
   *
   * @param userId        The user ID of the follower
   * @param targetUserId  The ID of the user being followed
   * @return A future of an option representing a relationship if it exists.
   */
  def getFollow(userId: UUID, targetUserId: UUID): Future[Option[Follow]] = {
    val query = followTableQuery
      .filter(follow => follow.userId === userId && follow.targetUserId === targetUserId)
      .join(userTableQuery)
      .on(_.userId === _.id)
      .join(userTableQuery)
      .on(_._1.targetUserId === _.id)
    db.run(query.result.headOption) map {
      raw =>
        raw map {
          case (((_, _, whenCreated), followingUser), targetUser) =>
            Follow(followingUser.getPublicUserProfile, targetUser.getPublicUserProfile, whenCreated)
        }

    }
  }

  /**
   * Unfollows a user
   *
   * @param userId       The user ID of the current follower
   * @param targetUserId The ID of the user currently being followed
   * @return
   */
  def unfollowUser(userId: UUID, targetUserId: UUID): Future[Unit] = {
    db.run(followTableQuery.filter(row => row.userId === userId && row.targetUserId === targetUserId).delete)
      .map(_ => ())

  }
}
