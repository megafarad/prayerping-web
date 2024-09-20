package org.prayerping.services

import java.util.UUID
import scala.concurrent.Future

trait ChannelAuthService {
  /**
   * Checks if the user is authorized to receive events on a given channel
   *
   * @param channel The channel to check for authorization
   * @param userId  The user ID to check for authorization
   * @return  A future value of true if authorized, false if not
   */
  def isAuthorized(channel: String, userId: UUID): Future[Boolean]
}
