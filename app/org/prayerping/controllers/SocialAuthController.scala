package org.prayerping.controllers

import org.prayerping.services.AuthenticateService
import play.silhouette.api._
import play.silhouette.api.exceptions.ProviderException
import play.silhouette.impl.providers._

import javax.inject.Inject
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Request }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * The social auth controller.
 */
class SocialAuthController @Inject() (
  scc: SilhouetteControllerComponents,
  authenticateService: AuthenticateService
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  /**
   * Authenticates a user against a social provider.
   *
   * @param provider The ID of the provider to authenticate against.
   * @return The result to display.
   */
  def authenticate(provider: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- authenticateService.provideUserForSocialAccount(provider, profile, authInfo)
            _ <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- authenticatorService.create(profile.loginInfo)
            value <- authenticatorService.init(authenticator)
            result <- authenticatorService.embed(value, Redirect(routes.ApplicationController.index))
          } yield {
            eventBus.publish(LoginEvent(user, request))
            result
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect("/signIn", Map("error" -> Seq(Messages("could.not.authenticate"))))
    }
  }

  def providers: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(Json.toJson(socialProviderRegistry.providers.map(_.id)))
  }
}
