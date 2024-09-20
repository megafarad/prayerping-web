package org.prayerping.models.daos

import org.prayerping.models.{ PrayerRequestReaction, ReactionType }
import PostgresProfile.api._
import com.fasterxml.uuid.Generators
import play.api.db.slick.DatabaseConfigProvider

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PrayerRequestReactionDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends PrayerRequestReactionDAO with DAOSlick {

  /**
   * Gets reactions for a prayer request
   *
   * @param prayerRequestId The ID of the prayer request
   * @return A future of a sequence of prayer request reactions
   */
  def getPrayerRequestReactions(prayerRequestId: UUID): Future[Seq[PrayerRequestReaction]] = {
    val query = prayerRequestReactionTableQuery.filter(_.requestId === prayerRequestId).join(userTableQuery).on(_.userId === _.id)
    db.run(query.result) map {
      results =>
        results map {
          case (row, user) => PrayerRequestReaction(row.id, row.requestId, user.getPublicUserProfile, row.reactionType)
        }
    }
  }

  def getPrayerRequestReaction(reactionId: UUID): Future[Option[PrayerRequestReaction]] = {
    val query = prayerRequestReactionTableQuery.filter(_.id === reactionId).join(userTableQuery).on(_.userId === _.id)
    db.run(query.result.headOption) map {
      result =>
        result map {
          case (row, user) => PrayerRequestReaction(row.id, row.requestId, user.getPublicUserProfile, row.reactionType)
        }
    }
  }

  /**
   * Creates a prayer request reaction
   *
   * @param prayerRequestId The ID of the prayer request
   * @param userId          The user of the reaction
   * @param reactionType    The reaction
   * @return A future of the prayer request reaction
   */
  def createPrayerRequestReaction(prayerRequestId: UUID, userId: UUID, reactionType: ReactionType.Value): Future[PrayerRequestReaction] = {
    val newRow = PrayerRequestReactionRow(Generators.timeBasedReorderedGenerator().generate(), prayerRequestId, userId, reactionType)
    db.run(prayerRequestReactionTableQuery += newRow) flatMap {
      _ => getPrayerRequestReaction(newRow.id).map(_.get)
    }

  }

  /**
   * Deletes a prayer request reaction
   *
   * @param prayerRequestReactionId The ID of the reaction to delete
   * @return A future that completes when the reaction has been deleted
   */
  def deletePrayerRequestReaction(prayerRequestReactionId: UUID): Future[Unit] = {
    val query = prayerRequestReactionTableQuery.filter(_.id === prayerRequestReactionId)
    db.run(query.delete).map(_ => ())
  }

}
