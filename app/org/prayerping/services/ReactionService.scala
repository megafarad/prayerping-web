package org.prayerping.services

import org.prayerping.models.{ PrayerRequestReaction, PrayerResponseReaction, ReactionType }

import java.util.UUID
import scala.concurrent.Future

trait ReactionService {
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
   * @param prayerRequestReaction The ID of the reaction to delete
   * @return A future that completes when the reaction has been deleted
   */
  def deletePrayerRequestReaction(prayerRequestReaction: PrayerRequestReaction): Future[Unit]

  /**
   * Gets a specific prayer response reaction
   *
   * @param id  The ID of the reaction
   * @return  A future of an option of a matching prayer response reaction
   */
  def getPrayerResponseReaction(id: UUID): Future[Option[PrayerResponseReaction]]

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
   * @param prayerResponseReaction The ID of the reaction to delete
   * @return A future that completes once the reaction has been deleted
   */
  def deletePrayerResponseReaction(prayerResponseReaction: PrayerResponseReaction): Future[Unit]

}
