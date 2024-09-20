package org.prayerping.models.daos

import org.prayerping.models.{ Page, PrayerResponse, User }
import PostgresProfile.api._
import com.fasterxml.uuid.Generators
import play.api.db.slick.DatabaseConfigProvider

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PrayerResponseDAOImpl @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends PrayerResponseDAO with DAOSlick {

  /**
   * Gets responses for a prayer request
   *
   * @param requestId The prayer request ID
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return A future of a paginated list of prayer responses
   */
  def getPrayerResponses(requestId: UUID, page: Int, pageSize: Int, requesterId: Option[UUID]): Future[Page[PrayerResponse]] = {
    val totalQuery = prayerResponseTableQuery.filter(_.requestId === requestId).join(userTableQuery)
      .on(_.userId === _.id).sortBy(_._1.whenCreated)
    getPrayerResponses(totalQuery, page, pageSize, requesterId)
  }

  /**
   * Performs a full-text searches for prayer responses of a particular prayer request
   *
   * @param requestId The prayer request ID
   * @param query     The search string
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return A future of a paginated list of matching prayer responses
   */
  def searchPrayerResponses(requestId: UUID, query: String, page: Int, pageSize: Int,
    requesterId: Option[UUID]): Future[Page[PrayerResponse]] = {
    val totalQuery = prayerResponseTableQuery.filter(r => r.requestId === requestId &&
      r.searchField @@ webSearchToTsQuery(query)).join(userTableQuery).on(_.userId === _.id).sortBy(_._1.whenCreated)
    getPrayerResponses(totalQuery, page, pageSize, requesterId)
  }

  private def getPrayerResponses(totalQuery: Query[(PrayerResponseTable, UserTable), (PrayerResponseRow, User), Seq], page: Int,
    pageSize: Int, requesterId: Option[UUID]) = {
    val offset = page * pageSize
    val paginatedQuery = totalQuery.drop(offset).take(pageSize)
    for {
      paginatedItems <- db.run(paginatedQuery.result) map {
        raw =>
          raw map {
            case (request, user) =>
              val canEdit = requesterId.contains(user.userID)
              PrayerResponse(request.id, request.requestId, user.getPublicUserProfile, request.response,
                request.whenCreated, canEdit)
          }
      }
      totalCount <- db.run(totalQuery.length.result)
    } yield Page(paginatedItems, page, offset, totalCount.toLong)

  }

  /**
   * Gets a specific prayer response
   *
   * @param responseId The ID of the prayer response
   * @return A future of an option of a prayer response
   */
  def getPrayerResponse(responseId: UUID, requesterId: Option[UUID]): Future[Option[PrayerResponse]] = {
    val query = prayerResponseTableQuery.filter(_.id === responseId).join(userTableQuery).on(_.userId === _.id)
    db.run(query.result.headOption) map {
      raw =>
        raw map {
          case (row, user) =>
            val canEdit = requesterId.contains(user.userID)
            PrayerResponse(row.id, row.requestId, user.getPublicUserProfile, row.response, row.whenCreated, canEdit)
        }
    }
  }

  /**
   * Creates a prayer response
   *
   * @param requestId   The ID of the prayer request
   * @param userId      The user ID of the responder
   * @param response    The text of the response
   * @return  A future of the created response
   */
  def createPrayerResponse(requestId: UUID, userId: UUID, response: String): Future[PrayerResponse] = {
    val newRow = PrayerResponseRow(Generators.timeBasedReorderedGenerator().generate(), requestId, userId, response, Instant.now())
    db.run(prayerResponseTableQuery += newRow) flatMap {
      _ => getPrayerResponse(newRow.id, Some(userId)).map(_.get)
    }
  }

  /**
   * Updates a prayer response
   *
   * @param responseId  The ID of the prayer response
   * @param response    The updated text of the response
   * @return    A future of the updated response
   */
  def updatePrayerResponse(responseId: UUID, response: String, requesterId: Option[UUID]): Future[PrayerResponse] = {
    val query = for { prayerResponse <- prayerResponseTableQuery if prayerResponse.id === responseId }
      yield prayerResponse.response
    db.run(query.update(response)).flatMap(_ => getPrayerResponse(responseId, requesterId).map(_.get))
  }

  /**
   * Deletes a prayer response
   *
   * @param responseId The ID of the prayer response to delete
   * @return A future that completes once the prayer response has been deleted
   */
  def deletePrayerResponse(responseId: UUID): Future[Unit] = {
    db.run(prayerResponseTableQuery.filter(_.id === responseId).delete).map(_ => ())
  }
}
