package org.prayerping.controllers

import org.prayerping.forms.ForgotPasswordForm
import play.silhouette.api._
import play.silhouette.impl.providers.CredentialsProvider

import javax.inject.Inject
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.mailer.Email
import play.api.mvc.{ Action, AnyContent, Request }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Forgot Password` controller.
 */
class ForgotPasswordController @Inject() (
  components: SilhouetteControllerComponents
)(implicit ex: ExecutionContext) extends SilhouetteController(components) {

  /**
   * Sends an email with password reset instructions.
   *
   * It sends an email to the given address if it exists in the database. Otherwise we do not show the user
   * a notice for not existing email addresses to prevent the leak of existing email addresses.
   *
   * @return The result to display.
   */
  def submit: Action[AnyContent] = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    ForgotPasswordForm.form.bindFromRequest().fold(
      _ => Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.request")))),
      email => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
        val result = Ok(Json.obj("info" -> Messages("reset.email.sent")))
        userService.retrieve(loginInfo).flatMap {
          case Some(user) if user.email.isDefined =>
            authTokenService.create(user.userID).map { authToken =>
              val url = getBaseUrl + "/password/reset/" + authToken.id

              mailerClient.send(Email(
                subject = Messages("email.reset.password.subject"),
                from = Messages("email.from"),
                to = Seq(email),
                bodyText = Some(org.prayerping.views.txt.emails.resetPassword(user, url).body),
                bodyHtml = Some(org.prayerping.views.html.emails.resetPassword(user, url).body)
              ))
              result
            }
          case _ => Future.successful(result)
        }
      }
    )
  }
}
