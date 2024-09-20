package org.prayerping.utils.auth

import org.prayerping.models.User
import org.prayerping.services.AuthenticateService
import play.api.mvc.Request
import play.silhouette.api.{ Authenticator, Authorization }

import javax.inject.Inject
import scala.concurrent.Future

class HasSignUpMethod @Inject() (authenticateService: AuthenticateService) {

  case class HasMethod[A <: Authenticator](provider: String) extends Authorization[User, A] {
    def isAuthorized[B](identity: User, authenticator: A)(implicit request: Request[B]): Future[Boolean] =
      authenticateService.userHasAuthenticationMethod(identity.userID, provider)
  }

  def apply[A <: Authenticator](provider: String): Authorization[User, A] = HasMethod[A](provider)

}
