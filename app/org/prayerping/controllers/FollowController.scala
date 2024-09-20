package org.prayerping.controllers

import org.prayerping.services.FollowService
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FollowController @Inject() (
  components: SilhouetteControllerComponents,
  followService: FollowService)(implicit ec: ExecutionContext) extends SilhouetteController(components) {

  def getUserFollow(targetUserId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    followService.getFollow(request.identity.userID, targetUserId) map {
      case Some(follow) => Ok(Json.toJson(follow))
      case None => NotFound
    }
  }
}
