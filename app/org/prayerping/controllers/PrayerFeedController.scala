package org.prayerping.controllers

import org.prayerping.services.PrayerFeedService
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PrayerFeedController @Inject() (
  components: SilhouetteControllerComponents,
  prayerFeedService: PrayerFeedService)(implicit ec: ExecutionContext) extends SilhouetteController(components) {

  def getPrayerRequestFeed(page: Int, pageSize: Int): Action[AnyContent] = SecuredAction.async { implicit request =>
    prayerFeedService.getPrayerRequestFeed(request.identity.userID, page, pageSize) map {
      feed => Ok(Json.toJson(feed))
    }
  }
}
