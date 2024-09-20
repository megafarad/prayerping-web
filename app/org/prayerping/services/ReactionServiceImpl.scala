package org.prayerping.services

import com.google.inject.Inject
import org.prayerping.models.daos.{ PrayerRequestReactionDAO, PrayerResponseReactionDAO }
import org.prayerping.models.{ DeletePrayerRequestReaction, DeletePrayerResponseReaction, NewPrayerRequestReaction, NewPrayerResponseReaction, PrayerRequestReaction, PrayerResponseReaction, ReactionType, WebSocketMessage }
import org.prayerping.providers.RedisClientProvider
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }

class ReactionServiceImpl @Inject() (
  prayerRequestReactionDAO: PrayerRequestReactionDAO,
  prayerResponseReactionDAO: PrayerResponseReactionDAO,
  redisClientProvider: RedisClientProvider)(implicit ec: ExecutionContext)
  extends ReactionService {

  private val redisClient = redisClientProvider.get()

  /**
   * Gets a specific prayer request reaction
   *
   * @param id The ID of the reaction
   * @return A future of an option of a reaction
   */
  def getPrayerRequestReaction(id: UUID): Future[Option[PrayerRequestReaction]] =
    prayerRequestReactionDAO.getPrayerRequestReaction(id)

  /**
   * Gets reactions for a prayer request
   *
   * @param prayerRequestId The ID of the prayer request
   * @return A future of a sequence of prayer request reactions
   */
  def getPrayerRequestReactions(prayerRequestId: UUID): Future[Seq[PrayerRequestReaction]] =
    prayerRequestReactionDAO.getPrayerRequestReactions(prayerRequestId)

  /**
   * Creates a prayer request reaction
   *
   * @param prayerRequestId The ID of the prayer request
   * @param userId          The user of the reaction
   * @param reactionType    The reaction
   * @return A future of the prayer request reaction
   */
  def createPrayerRequestReaction(prayerRequestId: UUID, userId: UUID, reactionType: ReactionType.Value): Future[PrayerRequestReaction] = {
    for {
      reaction <- prayerRequestReactionDAO.createPrayerRequestReaction(prayerRequestId, userId, reactionType)
      webSocketMessage = Json.toJson[WebSocketMessage](NewPrayerRequestReaction(reaction)).toString()
      _ <- redisClient.publish(s"prayer.${prayerRequestId}.reactions", webSocketMessage)
    } yield reaction
  }

  /**
   * Deletes a prayer request reaction
   *
   * @param prayerRequestReaction The ID of the reaction to delete
   * @return A future that completes when the reaction has been deleted
   */
  def deletePrayerRequestReaction(prayerRequestReaction: PrayerRequestReaction): Future[Unit] = {
    val webSocketMessage = Json.toJson[WebSocketMessage](DeletePrayerRequestReaction(prayerRequestReaction)).toString()
    for {
      _ <- prayerRequestReactionDAO.deletePrayerRequestReaction(prayerRequestReaction.id)
      _ <- redisClient.publish(s"prayer.${prayerRequestReaction.requestId}.reactions", webSocketMessage)
    } yield ()
  }

  /**
   * Gets a specific prayer response reaction
   *
   * @param id The ID of the reaction
   * @return A future of an option of a matching prayer response reaction
   */
  def getPrayerResponseReaction(id: UUID): Future[Option[PrayerResponseReaction]] =
    prayerResponseReactionDAO.getPrayerResponseReaction(id)

  /**
   * Gets reactions for a prayer response
   *
   * @param prayerResponseId The ID of the prayer response
   * @return A future of a sequence of prayer response reactions
   */
  def getPrayerResponseReactions(prayerResponseId: UUID): Future[Seq[PrayerResponseReaction]] =
    prayerResponseReactionDAO.getPrayerResponseReactions(prayerResponseId)

  /**
   * Creates a prayer response reaction
   *
   * @param prayerResponseId The ID of the prayer response
   * @param userId           The ID of the user for the reaction
   * @param reactionType     The reaction
   * @return A future of the prayer response reaction
   */
  def createPrayerResponseReaction(prayerResponseId: UUID, userId: UUID, reactionType: ReactionType.Value): Future[PrayerResponseReaction] = {
    for {
      prayerResponseReaction <- prayerResponseReactionDAO.createPrayerResponseReaction(prayerResponseId, userId, reactionType)
      webSocketMessage = Json.toJson[WebSocketMessage](NewPrayerResponseReaction(prayerResponseReaction)).toString()
      _ <- redisClient.publish(s"response.$prayerResponseId.reactions", webSocketMessage)
    } yield prayerResponseReaction
  }

  /**
   * Deletes a prayer response reaction
   *
   * @param prayerResponseReaction The ID of the reaction to delete
   * @return A future that completes once the reaction has been deleted
   */
  def deletePrayerResponseReaction(prayerResponseReaction: PrayerResponseReaction): Future[Unit] = {
    val webSocketMessage = Json.toJson[WebSocketMessage](DeletePrayerResponseReaction(prayerResponseReaction)).toString()
    for {
      _ <- prayerResponseReactionDAO.deletePrayerResponseReaction(prayerResponseReaction.id)
      _ <- redisClient.publish(s"response.${prayerResponseReaction.responseId}.reactions", webSocketMessage)
    } yield ()
  }

}
