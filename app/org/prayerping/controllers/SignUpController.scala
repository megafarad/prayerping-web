package org.prayerping.controllers

import org.prayerping.forms.SignUpForm
import org.prayerping.models.User
import com.fasterxml.uuid.Generators
import org.prayerping.services.{ AuthenticateService, CryptoService }
import org.prayerping.services.captcha.CaptchaService
import play.api.Logging
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.mailer.Email
import play.api.mvc._
import play.silhouette.api._
import play.silhouette.impl.providers._

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The `Sign Up` controller.
 */
class SignUpController @Inject() (
  components: SilhouetteControllerComponents,
  authenticateService: AuthenticateService,
  captchaService: CaptchaService,
  cryptoService: CryptoService
)(implicit ex: ExecutionContext) extends SilhouetteController(components) with Logging {

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def post: Action[AnyContent] = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest().fold(
      err => {
        logger.info("Invalid sign up request: " + err)
        Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.request"))))
      },
      data =>
        captchaService.validate(data.captchaResponse, request.remoteAddress).flatMap {
          valid =>
            if (valid) {
              val result = Ok(Json.obj("info" -> Messages("sign.up.email.sent", data.email)))
              val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
              userService.retrieve(loginInfo).flatMap {
                case Some(user) =>
                  val signInUrl = getBaseUrl + "/signIn"
                  val forgotPasswordUrl = getBaseUrl + "/password/forgot"
                  mailerClient.send(Email(
                    subject = Messages("email.already.signed.up.subject"),
                    from = Messages("email.from"),
                    to = Seq(data.email),
                    bodyText = Some(org.prayerping.views.txt.emails.alreadySignedUp(user, signInUrl, forgotPasswordUrl).body),
                    bodyHtml = Some(org.prayerping.views.html.emails.alreadySignedUp(user, signInUrl, forgotPasswordUrl).body)
                  ))

                  Future.successful(result)
                case None =>
                  val authInfo = passwordHasherRegistry.current.hash(data.password)
                  val salt = cryptoService.generateSalt
                  val (publicKey, encryptedPrivateKey) = cryptoService.generateKeyPair(salt)
                  val user = User(
                    userID = Generators.timeBasedReorderedGenerator().generate(),
                    handle = data.handle,
                    domain = None,
                    name = Some(data.name),
                    faithTradition = data.faithTradition,
                    email = Some(data.email),
                    avatarURL = None,
                    profile = data.profile,
                    signedUpAt = Instant.now(),
                    activated = false,
                    publicKey = publicKey,
                    privateKey = Some(encryptedPrivateKey),
                    salt = Some(salt)
                  )
                  for {
                    avatar <- avatarService.retrieveURL(data.email)
                    user <- userService.save(user.copy(avatarURL = avatar))
                    _ <- authenticateService.addAuthenticateMethod(user.userID, loginInfo, authInfo)
                    authToken <- authTokenService.create(user.userID)
                  } yield {
                    val url = getBaseUrl(request) + "/account/activate/" + authToken.id
                    mailerClient.send(Email(
                      subject = Messages("email.sign.up.subject"),
                      from = Messages("email.from"),
                      to = Seq(data.email),
                      bodyText = Some(org.prayerping.views.txt.emails.signUp(user, url).body),
                      bodyHtml = Some(org.prayerping.views.html.emails.signUp(user, url).body)
                    ))

                    eventBus.publish(SignUpEvent(user, request))
                    result
                  }
              }
            } else {
              Future.successful(BadRequest(Json.obj("error" -> Messages("invalid.request"))))
            }
        }
    )
  }

}
