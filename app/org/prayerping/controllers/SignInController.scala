package org.prayerping.controllers

import org.prayerping.forms.{ SignInForm, TotpForm }
import org.prayerping.forms.TotpForm.Data
import play.silhouette.api.exceptions.ProviderException
import play.silhouette.api.util.Credentials
import play.silhouette.impl.exceptions.IdentityNotFoundException
import play.silhouette.impl.providers._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc.{ Action, AnyContent, Request }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Sign In` controller.
 */
class SignInController @Inject() (
  scc: SilhouetteControllerComponents
)(implicit ex: ExecutionContext) extends AbstractAuthController(scc) {

  implicit val formFormat: Format[Data] = Json.format[TotpForm.Data]
  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def post: Action[AnyContent] = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignInForm.form.bindFromRequest().fold(
      _ => Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.request")))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) if !user.activated =>
              Future.successful(BadRequest(Json.obj("error" -> "User not active")))
            case Some(user) =>
              userService.retrieveUserLoginInfo(user.userID, GoogleTotpProvider.ID) flatMap {
                case Some((_, googleTotpLoginInfo)) =>
                  authInfoRepository.find[GoogleTotpInfo](googleTotpLoginInfo).flatMap {
                    case Some(totpInfo) => Future.successful(Ok(Json.obj("totpChallenge" -> TotpForm.Data(
                      user.userID, totpInfo.sharedKey, data.rememberMe))))
                    case _ => authenticateUser(user, loginInfo, data.rememberMe)
                  }
                case None => authenticateUser(user, loginInfo, data.rememberMe)
              }

            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            Forbidden(Json.obj("error" -> Messages("invalid.credentials")))
        }
      }
    )
  }
}
