package org.prayerping.models.daos

import org.prayerping.models.{ PrayerRequestReaction, ReactionType }

import java.util.UUID
import scala.concurrent.Future

trait PrayerRequestReactionDAO {

  /**
   * Gets a specific prayer request reaction
   *
   * @param id The ID of the reaction
   * @return  A future of an option of a reaction
   */
  def getPrayerRequestReaction(id: UUID): Future[Option[PrayerRequestReaction]]

  /**
   * Gets reactions for a prayer request
   *
   * @param prayerRequestId The ID of the prayer request
   * @return A future of a sequence of prayer request reactions
   */
  def getPrayerRequestReactions(prayerRequestId: UUID): Future[Seq[PrayerRequestReaction]]

  /**
   * Creates a prayer request reaction
   *
   * @param prayerRequestId   The ID of the prayer request
   * @param userId            The user of the reaction
   * @param reactionType      The reaction
   * @return      A future of the prayer request reaction
   */
  def createPrayerRequestReaction(prayerRequestId: UUID, userId: UUID, reactionType: ReactionType.Value): Future[PrayerRequestReaction]

  /**
   * Deletes a prayer request reaction
   *
   * @param prayerRequestReactionId The ID of the reaction to delete
   * @return A future that completes when the reaction has been deleted
   */
  def deletePrayerRequestReaction(prayerRequestReactionId: UUID): Future[Unit]
}
