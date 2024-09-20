package org.prayerping.models.daos

import com.fasterxml.uuid.Generators
import org.prayerping.models.daos.PostgresProfile.api._
import org.prayerping.models.{Visibility, _}
import org.prayerping.utils._
import org.prayerping.utils.config.AnonymousUserConfig
import play.api.Logging
import play.api.db.slick.DatabaseConfigProvider

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PrayerRequestDAOImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  anonymousUserConfig: AnonymousUserConfig)(implicit ec: ExecutionContext) extends PrayerRequestDAO with DAOSlick
  with Logging {

  /**
   * Gets a paginated list of prayer requests, ordered by most recent
   *
   * @param page     The page of the output
   * @param pageSize The page size in the output
   * @return A future of a [[Page]] of [[PrayerRequest]]s
   */
  def getPrayerRequests(page: Int, pageSize: Int, requesterId: Option[UUID]): Future[Page[PrayerRequest]] = {
    val query = prayerRequestTableQuery
      .join(userTableQuery)
      .on(_.userId === _.id)
      .sortBy(_._1.whenCreated.desc)
    getPrayerRequests(query, page, pageSize, requesterId)
  }

  /**
   * Gets a feed of followed user's prayer requests as well as group prayer requests
   * the user belongs to.
   *
   * @param userId   The ID of the user
   * @param page     The page of the output
   * @param pageSize The page size in the output
   * @return A future of a page of prayer requests
   */
  def getPrayerRequestFeed(userId: UUID, page: Int, pageSize: Int): Future[Page[PrayerRequest]] = {
    val ownPrayersQuery = prayerRequestTableQuery.filter(_.userId === userId)
    val followsQuery = followTableQuery.filter(_.userId === userId)
      .join(prayerRequestTableQuery)
      .on(_.targetUserId === _.userId)
      .map {
        case (_, prayerRequest) => prayerRequest
      }
    val prayerGroupsQuery = prayerGroupMembershipTableQuery.filter(_.userId === userId)
      .join(prayerRequestGroupTableQuery)
      .on(_.groupId === _.groupId)
      .join(prayerRequestTableQuery)
      .on(_._2.requestId === _.id)
      .map {
        case (_, prayerRequest) => prayerRequest
      }

    val fullQuery = (ownPrayersQuery union followsQuery union prayerGroupsQuery)
      .join(userTableQuery).on(_.userId === _.id)
      .sortBy(_._1.whenCreated.desc)

    getPrayerRequests(fullQuery, page, pageSize, Some(userId))
  }

  /**
   * Performs a full-text search of prayer requests
   *
   * @param query    The search string
   * @param page     The page of the search results
   * @param pageSize The page size in the search results
   * @return A future of a page of search results
   */
  def searchPrayerRequests(query: String, page: Int, pageSize: Int, requester: Option[UUID]): Future[Page[PrayerRequest]] = {
    val totalQuery = prayerRequestTableQuery.filter(_.searchField @@ webSearchToTsQuery(query))
      .join(userTableQuery)
      .on(_.userId === _.id)
      .sortBy(_._1.whenCreated.desc)
    getPrayerRequests(totalQuery, page, pageSize, requester)
  }

  /**
   * Gets a user's prayer requests
   *
   * @param handle   The handle of the user
   * @param page     The page of the output
   * @param pageSize The page size in the output
   * @return A future of a page of prayer requests of a user
   */
  def getUserPrayerRequests(handle: String, domain: Option[String], page: Int, pageSize: Int,
    includeAnonymous: Boolean, requester: Option[UUID]): Future[Page[PrayerRequest]] = {
    val userPrayerRequests = if (anonymousUserConfig.handle.equalsIgnoreCase(handle) && domain.isEmpty) {
      prayerRequestTableQuery
        .filter(_.isAnonymous)
        .join(userTableQuery)
        .on(_.userId === _.id)
        .sortBy(_._1.whenCreated.desc)
    } else {
      val query = userTableQuery
        .filter(_.handle === handle)
        .filterOpt(domain)(_.domain === _)
        .join(prayerRequestTableQuery)
        .on(_.id === _.userId)
        .sortBy {
          case (_, prayerRequestTable) => prayerRequestTable.whenCreated.desc
        }
        .map {
          case (user, prayerRequestTable) => (prayerRequestTable, user)
        }
      if (includeAnonymous) query else query.filter(!_._1.isAnonymous)
    }

    getPrayerRequests(userPrayerRequests, page, pageSize, requester)
  }

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
    includeAnonymous: Boolean, requester: Option[UUID]): Future[Page[PrayerRequest]] = {
    val matchingPrayerRequests = userTableQuery
      .filter(_.handle === handle)
      .filterOpt(domain)(_.domain === _)
      .join(prayerRequestTableQuery)
      .on(_.id === _.userId)
      .filter {
        case (_, prayerRequestTable) => prayerRequestTable.searchField @@ webSearchToTsQuery(query)
      }
      .sortBy {
        case (_, prayerRequestTable) => prayerRequestTable.whenCreated.desc
      }
      .map {
        case (user, prayerRequestTable) => (prayerRequestTable, user)
      }
    val factorAnonymous = if (includeAnonymous) matchingPrayerRequests else matchingPrayerRequests.filter(
      !_._1.isAnonymous)
    getPrayerRequests(factorAnonymous, page, pageSize, requester)
  }

  /**
   * Gets a paginated list of prayer requests by their group
   *
   * @param groupId  The ID of the group
   * @param page     The page of the output
   * @param pageSize The page size in the output
   * @return A future of a page of prayer requests for the group
   */
  def getGroupPrayerRequests(groupId: UUID, page: Int, pageSize: Int,
    requester: Option[UUID]): Future[Page[PrayerRequest]] = {
    val groupPrayerRequests = prayerRequestGroupTableQuery
      .filter(_.groupId === groupId)
      .join(prayerRequestTableQuery)
      .on(_.requestId === _.id)
      .join(userTableQuery)
      .on(_._2.userId === _.id)
      .sortBy {
        case ((_, prayerRequestTable), _) => prayerRequestTable.whenCreated.desc
      }
      .map {
        case ((_, prayerRequestTable), user) => (prayerRequestTable, user)
      }
    getPrayerRequests(groupPrayerRequests, page, pageSize, requester)
  }

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
    requester: Option[UUID]): Future[Page[PrayerRequest]] = {
    val matchingPrayerRequests = prayerRequestGroupTableQuery
      .filter(_.groupId === groupId)
      .join(prayerRequestTableQuery)
      .on(_.requestId === _.id)
      .join(userTableQuery)
      .on(_._2.userId === _.id)
      .filter {
        case ((_, prayerRequestTable), _) => prayerRequestTable.searchField @@ webSearchToTsQuery(query)
      }
      .sortBy {
        case ((_, prayerRequestTable), _) => prayerRequestTable.whenCreated.desc
      }
      .map {
        case ((_, prayerRequestTable), user) => (prayerRequestTable, user)
      }
    getPrayerRequests(matchingPrayerRequests, page, pageSize, requester)
  }

  /**
   * Shares a prayer request with a group
   *
   * @param requestId The ID of the prayer request
   * @param groupId   The ID of the prayer group
   * @return A future of an object representing the share
   */
  def sharePrayerRequestWithGroup(requestId: UUID, groupId: UUID): Future[PrayerRequestGroup] = {
    val newRow = PrayerRequestGroup(requestId, groupId)
    db.run(prayerRequestGroupTableQuery += newRow).map(_ => newRow)
  }

  /**
   * Removes a prayer request from a group
   *
   * @param requestId The ID of the prayer request
   * @param groupId   The ID of the prayer group
   * @return
   */
  def removePrayerRequestFromGroup(requestId: UUID, groupId: UUID): Future[Unit] = {
    db.run(prayerRequestGroupTableQuery.filter(row => row.requestId === requestId && row.groupId === groupId).delete)
      .map(_ => ())
  }

  /**
   * Gets a specific prayer request
   *
   * @param id The ID of the request to get
   * @return A future of an option of a [[PrayerRequest]]
   */
  def getPrayerRequest(id: UUID, requesterId: Option[UUID]): Future[Option[PrayerRequest]] = {
    val filtered = prayerRequestTableQuery.filter(_.id === id)
      .join(userTableQuery)
      .on(_.userId === _.id)
      .joinLeft(prayerRequestMentionTableQuery)
      .on(_._1.id === _.requestId)

    val withMentions = filtered.result.map {
      rows =>
        rows.groupBy {
          case (requestFields, _) => requestFields
        }.view.mapValues { values =>
          values.flatMap {
            case (_, Some(mentionRow)) => Seq(mentionRow)
            case (_, None) => Nil
          }
        }
    }.map {
      result =>
        result map {
          case ((row, user), mentionRows) =>
            val publicUser = if (row.isAnonymous) anonymousUserConfig.anonymousUser.getPublicUserProfile else
              user.getPublicUserProfile
            val canEdit = requesterId.contains(user.userID)
            val mentions = mentionRows.map {
              mentionRow =>
                mentionRow.domain match {
                  case Some(value) => s"@${mentionRow.handle}@$value"
                  case None => s"@${mentionRow.handle}"
                }
            }.toSet
            PrayerRequest(row.id, publicUser, row.request, row.isAnonymous, row.whenCreated, row.visibility, mentions,
              canEdit)

        }
    }
    db.run(withMentions).map(_.headOption)
  }

  def getMentionsForPrayerRequest(prayerRequestId: UUID): Future[Set[(String, Option[String])]] = {
    val existingMentionsQuery = prayerRequestMentionTableQuery.filter(_.requestId === prayerRequestId).map {
      mentionTable => (mentionTable.handle, mentionTable.domain)
    }
    db.run(existingMentionsQuery.result).map(_.toSet)
  }

  private def getPrayerRequests(
    totalQuery: Query[(PrayerRequestTable, UserTable), (PrayerRequestRow, User), Seq],
    page: Int, pageSize: Int, requesterId: Option[UUID]): Future[Page[PrayerRequest]] = {

    val offset = page * pageSize
    val paginatedQuery = totalQuery.drop(offset).take(pageSize)

    val withMentions = paginatedQuery
      .joinLeft(prayerRequestMentionTableQuery)
      .on(_._1.id === _.requestId)
      .result.map {
        rows =>
          rows.groupBy {
            case (requestFields, _) => requestFields
          }.view.mapValues { values =>
            values.flatMap {
              case (_, Some(mentionRow)) => Seq(mentionRow)
              case (_, None) => Nil
            }
          }
      }.map {
        result =>
          result map {
            case ((row, user), mentionRows) =>
              val publicUser = if (row.isAnonymous) anonymousUserConfig.anonymousUser.getPublicUserProfile else
                user.getPublicUserProfile
              val canEdit = requesterId.contains(user.userID)
              val mentions = mentionRows.map {
                mentionRow =>
                  mentionRow.domain match {
                    case Some(value) => s"@${mentionRow.handle}@$value"
                    case None => s"@${mentionRow.handle}"
                  }
              }.toSet
              PrayerRequest(row.id, publicUser, row.request, row.isAnonymous, row.whenCreated, row.visibility, mentions,
                canEdit)
          }
      }

    for {
      paginatedItems <- db.run(withMentions).map(_.toSeq)
      totalItems <- db.run(totalQuery.length.result)
    } yield Page(paginatedItems, page, offset, totalItems.toLong)

  }

  /**
   * Creates a new prayer request
   *
   * @param userId  The ID of the requesting user
   * @param request The text of the request
   * @return A future of the new prayer request
   */
  def createNewPrayerRequest(userId: UUID, request: String, isAnonymous: Boolean, visibility: Visibility.Value,
    mentions: Set[String]): Future[PrayerRequest] = {
    val newPrayerRequestRow = PrayerRequestRow(Generators.timeBasedReorderedGenerator().generate(), userId, request, isAnonymous,
      Instant.now(), visibility)
    val newMentionRows = mentions flatMap {
      mention =>
        parseMention(mention) map {
          case (handle, domain) => PrayerRequestMentionRow(
            Generators.timeBasedReorderedGenerator().generate(),
            newPrayerRequestRow.id, handle, domain, Instant.now())
        }
    }
    db.run(DBIO.seq(
      prayerRequestTableQuery += newPrayerRequestRow,
      prayerRequestMentionTableQuery ++= newMentionRows).transactionally) flatMap {
      _ => getPrayerRequest(newPrayerRequestRow.id, Some(userId)).map(_.get)
    }
  }

  /**
   * Updates an existing prayer request
   *
   * @param id      The ID of the prayer request
   * @param request The updated text of the request
   * @return A future of the updated prayer request
   */
  def updatePrayerRequest(id: UUID, request: String, isAnonymous: Boolean, visibility: Visibility.Value,
                          mentionRowTuplesToAdd: Seq[(UUID, UUID, String, Option[String], Instant)],
                          mentionsToRemove: Set[(String, Option[String])],
                          requesterId: Option[UUID]): Future[PrayerRequest] = {
    val query = for { prayerRequest <- prayerRequestTableQuery if prayerRequest.id === id }
      yield (prayerRequest.request, prayerRequest.isAnonymous, prayerRequest.visibility)
    val queryUpdate = query.update((request, isAnonymous, visibility))

    val mentionsToRemoveActions = mentionsToRemove.map {
      case (handle, domain) => prayerRequestMentionTableQuery.filter(mention => mention.requestId === id
        && mention.handle === handle && mention.domain === domain).delete
    }.toSeq

    val mentionRowsToAdd = mentionRowTuplesToAdd.map {
      tuple => (PrayerRequestMentionRow.apply _).tupled(tuple)
    }

    val addMentionAction = prayerRequestMentionTableQuery ++= mentionRowsToAdd

    db.run(DBIO.seq(DBIO.sequence(mentionsToRemoveActions), addMentionAction, queryUpdate).transactionally)
      .flatMap(_ => getPrayerRequest(id, requesterId).map(_.get))

  }

  /**
   * Deletes a prayer request
   *
   * @param id The ID of the request to delete
   * @return A future that completes once the prayer request has been deleted
   */
  def deletePrayerRequest(id: UUID): Future[Unit] =
    db.run(prayerRequestTableQuery.filter(_.id === id).delete).map(_ => ())

}
