package org.prayerping.services

import org.prayerping.models.WebSocketMessage._
import org.prayerping.models.{ Visibility, _ }
import org.prayerping.models.daos._
import org.prayerping.providers.RedisClientProvider
import org.prayerping.utils._
import play.api.libs.json.Json

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PrayerResponseServiceImpl @Inject() (
  prayerResponseDAO: PrayerResponseDAO,
  redisClientProvider: RedisClientProvider)(implicit ec: ExecutionContext)
  extends PrayerResponseService {

  private val redisClient = redisClientProvider.get()

  /**
   * Gets responses for a prayer request
   *
   * @param requestId The prayer request ID
   * @param page      The page of the output
   * @param pageSize  The page size in the output
   * @return A future of a paginated list of prayer responses
   */
  def getPrayerResponses(requestId: UUID, page: Int, pageSize: Int, requesterId: Option[UUID]): Future[Page[PrayerResponse]] =
    prayerResponseDAO.getPrayerResponses(requestId, page, pageSize, requesterId)

  /**
   * Gets a specific prayer response
   *
   * @param responseId The ID of the prayer response
   * @return A future of an option of a prayer response
   */
  def getPrayerResponse(responseId: UUID, requesterId: Option[UUID]): Future[Option[PrayerResponse]] =
    prayerResponseDAO.getPrayerResponse(responseId, requesterId)

  /**
   * Creates a prayer response
   *
   * @param requestId The ID of the prayer request
   * @param userId    The user ID of the responder
   * @param response  The text of the response
   * @return A future of the created response
   */
  def createPrayerResponse(requestId: UUID, userId: UUID, response: String): Future[PrayerResponse] = {
    for {
      prayerResponse <- prayerResponseDAO.createPrayerResponse(requestId, userId, response)
      webSocketMessage = Json.toJson[WebSocketMessage](NewPrayerResponse(prayerResponse)).toString()
      _ <- redisClient.publish(s"prayer.$requestId.response", webSocketMessage)
    } yield prayerResponse
  }

  /**
   * Updates a prayer response
   *
   * @param responseId The ID of the prayer response
   * @param response   The updated text of the response
   * @return A future of the updated response
   */
  def updatePrayerResponse(responseId: UUID, response: String, requesterId: Option[UUID]): Future[PrayerResponse] = {
    for {
      prayerResponse <- prayerResponseDAO.updatePrayerResponse(responseId, response, requesterId)
      webSocketMessage = Json.toJson[WebSocketMessage](UpdatePrayerResponse(prayerResponse)).toString()
      _ <- redisClient.publish(s"prayer.${prayerResponse.requestId}.response", webSocketMessage)
    } yield prayerResponse
  }

  /**
   * Deletes a prayer response
   *
   * @param response The ID of the prayer response to delete
   * @return A future that completes once the prayer response has been deleted
   */
  def deletePrayerResponse(response: PrayerResponse): Future[Unit] = {
    val webSocketMessage = Json.toJson[WebSocketMessage](DeletePrayerResponse(response)).toString()
    for {
      _ <- prayerResponseDAO.deletePrayerResponse(response.id)
      _ <- redisClient.publish(s"prayer.${response.requestId}.response", webSocketMessage)
    } yield ()
  }

}
