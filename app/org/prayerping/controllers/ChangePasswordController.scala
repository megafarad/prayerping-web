package org.prayerping.controllers

import org.prayerping.forms.ChangePasswordForm
import org.prayerping.utils.auth.{ DefaultEnv, HasSignUpMethod }
import org.prayerping.utils.auth.DefaultEnv
import play.silhouette.api.actions.SecuredRequest
import play.silhouette.api.exceptions.ProviderException
import play.silhouette.api.util.{ Credentials, PasswordInfo }
import play.silhouette.impl.providers.CredentialsProvider

import javax.inject.Inject
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Change Password` controller.
 */
class ChangePasswordController @Inject() (
  scc: SilhouetteControllerComponents,
  hasSignUpMethod: HasSignUpMethod
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  /**
   * Changes the password.
   *
   * @return The result to display.
   */
  def post: Action[AnyContent] = SecuredAction(hasSignUpMethod[AuthType](CredentialsProvider.ID)).async {
    implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
      ChangePasswordForm.form.bindFromRequest().fold(
        _ => Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.request")))),
        password => {
          val (currentPassword, newPassword) = password
          val credentials = Credentials(request.identity.email.getOrElse(""), currentPassword)
          credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
            val passwordInfo = passwordHasherRegistry.current.hash(newPassword)
            authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
              Ok(Json.obj("success" -> Messages("password.changed")))
            }
          }.recover {
            case _: ProviderException =>
              BadRequest(Json.obj("error" -> Messages("current.password.invalid")))
          }
        }
      )
  }
}
