package org.prayerping.controllers

import play.silhouette.api.util.PasswordInfo
import play.silhouette.impl.providers.{ GoogleTotpInfo, GoogleTotpProvider }
import play.api.libs.json._
import play.api.mvc.{ Action, AnyContent }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class UserController @Inject() (scc: SilhouetteControllerComponents)(implicit ex: ExecutionContext)
  extends AbstractAuthController(scc) {

  implicit val passwordInfoFormat: Format[PasswordInfo] = Json.format[PasswordInfo]
  implicit val totpInfoFormat: Format[GoogleTotpInfo] = Json.format[GoogleTotpInfo]

  def getLoggedInUser: Action[AnyContent] = SecuredAction.async { implicit request =>
    userService.retrieveUserLoginInfo(request.identity.userID, GoogleTotpProvider.ID).flatMap {
      case Some((user, loginInfo)) =>
        authInfoRepository.find[GoogleTotpInfo](loginInfo).map {
          case Some(value) => Ok(Json.obj("userProfile" -> Json.toJson(user.getUserProfile), "totpInfo" -> Json.toJson(value)))
          case None => Ok(Json.obj("userProfile" -> Json.toJson(user.getUserProfile)))
        }
      case _ => Future.successful(Ok(Json.obj("userProfile" -> Json.toJson(request.identity.getUserProfile))))
    }
  }

}
