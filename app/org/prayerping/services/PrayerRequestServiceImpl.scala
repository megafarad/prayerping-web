package org.prayerping.services

import com.google.inject.Inject
import org.prayerping.models.daos.{ FollowDAO, PrayerGroupDAO, PrayerRequestDAO }
import org.prayerping.models.{ DeletePrayerRequest, GroupHandle, Handle, NewPrayerRequest, Page, PrayerRequest, UpdatePrayerRequest, UserHandle, Visibility, WebSocketMessage }
import org.prayerping.providers.RedisClientProvider
import org.prayerping.utils.parseMentions
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }

class PrayerRequestServiceImpl @Inject() (
  prayerRequestDAO: PrayerRequestDAO,
  prayerGroupDAO: PrayerGroupDAO,
  followDAO: FollowDAO,
  handleService: HandleService,
  mentionService: MentionService,
  redisClientProvider: RedisClientProvider)(implicit ec: ExecutionContext) extends PrayerRequestService {
  private val redisClient = redisClientProvider.get()

  /**
   * Gets a paginated list of prayer requests, ordered by most recent
   *
   * @param page     The page of the output
   * @param pageSize The page size in the output
   * @return A future of a [[Page]] of [[PrayerRequest]]s
   */
  def getPrayerRequests(page: Int, pageSize: Int, requesterId: Option[UUID]): Future[Page[PrayerRequest]] =
    prayerRequestDAO.getPrayerRequests(page, pageSize, requesterId)

  /**
   * Gets a user's prayer requests
   *
   * @param handle   The handle of the user
   * @param domain   The domain of the user. None means it's a local account.
   * @param page     The page of the output
   * @param pageSize The page size in the output
   * @return A future of a page of prayer requests of a user
   */
  def getUserPrayerRequests(handle: String, domain: Option[String], page: Int, pageSize: Int,
    includeAnonymous: Boolean = false, requester: Option[UUID]): Future[Page[PrayerRequest]] =
    prayerRequestDAO.getUserPrayerRequests(handle, domain, page, pageSize, includeAnonymous, requester)
  /**
   * Gets a specific prayer request
   *
   * @param id The ID of the request to get
   * @return A future of an option of a [[PrayerRequest]]
   */
  def getPrayerRequest(id: UUID, requesterId: Option[UUID]): Future[Option[PrayerRequest]] = prayerRequestDAO
    .getPrayerRequest(id, requesterId)

  /**
   * Creates a new prayer request and notifies WebSocket followers
   *
   * @param userId  The ID of the requesting user
   * @param request The text of the request
   * @return A future of the new prayer request
   */
  def createPrayerRequest(userId: UUID, request: String, isAnonymous: Boolean,
    visibility: Visibility.Value): Future[PrayerRequest] = {
    for {
      mentionHandles <- extractMentions(request)
      mentions = mentionHandles map {
        case UserHandle(user) => user.mention
        case GroupHandle(group) => group.mention
      }
      prayerRequest <- prayerRequestDAO.createNewPrayerRequest(userId, request, isAnonymous, visibility, mentions)
      followers <- followDAO.getFollowers(userId)
      webSocketMessage = Json.toJson[WebSocketMessage](NewPrayerRequest(prayerRequest)).toString()
      followerChannels = followers.map(f => s"user.$f").toSet
      mentionUserChannels <- Future.sequence {
        mentionHandles map {
          case UserHandle(user) => Future.successful(Seq(s"user.${user.userID}"))
          case GroupHandle(group) => prayerGroupDAO.getPrayerGroupMembers(group.id)
            .map(_.map(membership => s"user.${membership.user.userID}"))
        }
      }.map(_.flatten)
      channels = visibility match {
        case Visibility.Public => Set("public.local") ++ followerChannels
        case Visibility.Unlisted => followerChannels ++ mentionUserChannels
        case Visibility.Private => followerChannels
        case Visibility.Direct => mentionUserChannels
        case _ => Set.empty
      }
    } yield prayerRequest

  }

  /**
   * Updates an existing prayer request
   *
   * @param id      The ID of the prayer request
   * @param request The updated text of the request
   * @return A future of the updated prayer request
   */
  def updatePrayerRequest(id: UUID, request: String, isAnonymous: Boolean, visibility: Visibility.Value,
    requesterId: Option[UUID]): Future[PrayerRequest] = {

    for {
      mentionHandles <- extractMentions(request)
      mentions = mentionHandles map {
        case UserHandle(user) => (user.handle, user.domain)
        case GroupHandle(group) => (group.handle, group.domain)
      }
      existingMentions <- prayerRequestDAO.getMentionsForPrayerRequest(id)
      (mentionsToAdd, mentionsToRemove) = mentionService.determineMentionsToAddAndRemove(mentions, existingMentions)
      mentionRowsToAdd = mentionService.createMentionRowsToAdd(mentionsToAdd, id)
      prayerRequest <- prayerRequestDAO.updatePrayerRequest(id, request, isAnonymous, visibility, mentionRowsToAdd, mentionsToRemove, requesterId)
      followers <- followDAO.getFollowers(prayerRequest.user.userID)
      webSocketMessage = Json.toJson[WebSocketMessage](UpdatePrayerRequest(prayerRequest)).toString()
      _ <- if (visibility == Visibility.Public) {
        redisClient.publish("public.local", webSocketMessage)
      } else Future.successful(())
      _ <- redisClient.publish(s"user.${prayerRequest.user.userID}", webSocketMessage)
    } yield prayerRequest
  }

  private def extractMentions(request: String): Future[Set[Handle]] = {
    Future.sequence {
      parseMentions(request) map {
        case (handle, domain) => handleService.getHandle(handle, domain)
      }
    }.map(_.flatten)
  }

  /**
   * Deletes a prayer request
   *
   * @param prayerRequest The ID of the request to delete
   * @return A future that completes once the prayer request has been deleted
   */
  def deletePrayerRequest(prayerRequest: PrayerRequest): Future[Unit] = {

    for {
      _ <- prayerRequestDAO.deletePrayerRequest(prayerRequest.id)
      followers <- followDAO.getFollowers(prayerRequest.user.userID)
      webSocketMessage = Json.toJson[WebSocketMessage](DeletePrayerRequest(prayerRequest.id)).toString()
      _ <- redisClient.publish("public.local", webSocketMessage)
      _ <- redisClient.publish(s"user.${prayerRequest.user.userID}", webSocketMessage)
      _ <- Future.sequence(followers map {
        follower => redisClient.publish(s"user.$follower", webSocketMessage)
      })
    } yield ()
  }
}