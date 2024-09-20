package org.prayerping.models.daos

import org.prayerping.models.{ PrayerResponseReaction, ReactionType }
import PostgresProfile.api._
import com.fasterxml.uuid.Generators
import play.api.db.slick.DatabaseConfigProvider

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PrayerResponseReactionDAOImpl @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends PrayerResponseReactionDAO with DAOSlick {

  /**
   * Gets reactions for a prayer response
   *
   * @param prayerResponseId The ID of the prayer response
   * @return A future of a sequence of prayer response reactions
   */
  def getPrayerResponseReactions(prayerResponseId: UUID): Future[Seq[PrayerResponseReaction]] = {
    val query = prayerResponseReactionTableQuery.filter(_.responseId === prayerResponseId).join(userTableQuery)
      .on(_.userId === _.id)
    db.run(query.result) map {
      results =>
        results map {
          case (row, user) => PrayerResponseReaction(row.id, row.responseId, user.getPublicUserProfile,
            row.reactionType)
        }
    }
  }

  /**
   * Creates a prayer response reaction
   *
   * @param prayerResponseId  The ID of the prayer response
   * @param userId            The ID of the user for the reaction
   * @param reactionType      The reaction
   * @return    A future of the prayer response reaction
   */
  def createPrayerResponseReaction(prayerResponseId: UUID, userId: UUID, reactionType: ReactionType.Value): Future[PrayerResponseReaction] = {
    val newRow = PrayerResponseReactionRow(Generators.timeBasedReorderedGenerator().generate(), prayerResponseId, userId, reactionType)
    db.run(prayerResponseReactionTableQuery += newRow) flatMap {
      _ => getPrayerResponseReaction(newRow.id).map(_.get)
    }
  }

  def getPrayerResponseReaction(reactionId: UUID): Future[Option[PrayerResponseReaction]] = {
    val query = prayerResponseReactionTableQuery.filter(_.id === reactionId).join(userTableQuery).on(_.userId === _.id)
    db.run(query.result.headOption) map {
      result =>
        result map {
          case (row, user) => PrayerResponseReaction(row.id, row.responseId, user.getPublicUserProfile,
            row.reactionType)
        }
    }
  }

  /**
   * Deletes a prayer response reaction
   *
   * @param prayerResponseReactionId The ID of the reaction to delete
   * @return A future that completes once the reaction has been deleted
   */
  def deletePrayerResponseReaction(prayerResponseReactionId: UUID): Future[Unit] = {
    val query = prayerResponseReactionTableQuery.filter(_.id === prayerResponseReactionId)
    db.run(query.delete).map(_ => ())
  }
}
