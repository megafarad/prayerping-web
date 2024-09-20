package org.prayerping.services

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class ChannelAuthServiceImpl @Inject() extends ChannelAuthService {

  /**
   * Checks if the user is authorized to receive events on a given channel
   *
   * @param channel The channel to check for authorization
   * @param userId  The user ID to check for authorization
   * @return A future value of true if authorized, false if not
   */
  def isAuthorized(channel: String, userId: UUID): Future[Boolean] = {

    val uuidRegex = "\\b[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\\b"
    val userChannelRegex = s"user\\.($uuidRegex)".r
    val prayerReactionsRegex = s"prayer\\.($uuidRegex)\\.reactions".r

    channel match {
      case "public.local" => Future.successful(true)
      case userChannelRegex(parsedUserId) => Future.successful(UUID.fromString(parsedUserId) == userId)
      case _ => Future.successful(false)
    }

  }
}
