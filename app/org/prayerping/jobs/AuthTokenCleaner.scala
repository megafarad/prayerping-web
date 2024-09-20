package org.prayerping.jobs

import javax.inject.Inject
import org.apache.pekko.actor._
import org.prayerping.jobs.AuthTokenCleaner.Clean
import org.prayerping.services.AuthTokenService
import play.silhouette.api.util.Clock
import play.api.Logging

import scala.concurrent.ExecutionContext

/**
 * A job which cleanup invalid auth tokens.
 *
 * @param service The auth token service implementation.
 * @param clock The clock implementation.
 */
class AuthTokenCleaner @Inject() (
  service: AuthTokenService,
  clock: Clock)(implicit ec: ExecutionContext)
  extends Actor with Logging {

  /**
   * Process the received messages.
   */
  def receive: Receive = {
    case Clean =>
      val start = clock.now.toInstant.toEpochMilli
      val msg = new StringBuffer()
      service.clean.map { deleted =>
        val seconds = (clock.now.toInstant.toEpochMilli - start) / 1000
        msg.append("Total of %s auth tokens(s) were deleted in %s seconds".format(deleted.length, seconds))
        logger.info(msg.toString)
      }.recover {
        case e =>
          msg.append("Couldn't cleanup auth tokens because of unexpected error")
          logger.error(msg.toString, e)
      }
  }
}

/**
 * The companion object.
 */
object AuthTokenCleaner {
  case object Clean
}
