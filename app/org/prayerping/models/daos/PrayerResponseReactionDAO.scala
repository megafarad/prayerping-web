package org.prayerping.models.daos

import org.prayerping.models.{ PrayerResponseReaction, ReactionType }

import java.util.UUID
import scala.concurrent.Future

trait PrayerResponseReactionDAO {

  /**
   *
   * @param reactionId
   * @return
   */
  def getPrayerResponseReaction(reactionId: UUID): Future[Option[PrayerResponseReaction]]

  /**
   * Gets reactions for a prayer response
   *
   * @param prayerResponseId The ID of the prayer response
   * @return A future of a sequence of prayer response reactions
   */
  def getPrayerResponseReactions(prayerResponseId: UUID): Future[Seq[PrayerResponseReaction]]

  /**
   * Creates a prayer response reaction
   *
   * @param prayerResponseId  The ID of the prayer response
   * @param userId            The ID of the user for the reaction
   * @param reactionType      The reaction
   * @return    A future of the prayer response reaction
   */
  def createPrayerResponseReaction(prayerResponseId: UUID, userId: UUID, reactionType: ReactionType.Value): Future[PrayerResponseReaction]

  /**
   * Deletes a prayer response reaction
   *
   * @param prayerResponseReactionId The ID of the reaction to delete
   * @return A future that completes once the reaction has been deleted
   */
  def deletePrayerResponseReaction(prayerResponseReactionId: UUID): Future[Unit]
}
