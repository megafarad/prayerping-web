package org.prayerping.services

import org.prayerping.models.User
import org.prayerping.models.daos.LoginInfoDAO
import play.silhouette.api.{ AuthInfo, LoginInfo }
import play.silhouette.api.repositories.AuthInfoRepository
import play.silhouette.impl.providers.CommonSocialProfile

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

class AuthenticateService @Inject() (
  authInfoRepository: AuthInfoRepository,
  loginInfoDAO: LoginInfoDAO,
  userService: UserService)(implicit ec: ExecutionContext) {

  def addAuthenticateMethod[T <: AuthInfo](userID: UUID, loginInfo: LoginInfo, authInfo: T): Future[Unit] = {
    for {
      _ <- loginInfoDAO.saveUserLoginInfo(userID, loginInfo)
      _ <- authInfoRepository.add(loginInfo, authInfo)
    } yield ()
  }

  def removeAuthenticateMethod[T <: AuthInfo](userID: UUID, loginInfo: LoginInfo)(implicit classTag: ClassTag[T]): Future[Unit] = {
    for {
      _ <- authInfoRepository.remove[T](loginInfo)
      _ <- loginInfoDAO.deleteUserLoginInfo(userID, loginInfo)
    } yield ()
  }

  def getAuthenticationProviders(email: String): Future[Seq[String]] = loginInfoDAO.getAuthenticationProviders(email)

  def userHasAuthenticationMethod(userId: UUID, providerId: String): Future[Boolean] =
    loginInfoDAO.find(userId, providerId).map(_.nonEmpty)

  def provideUserForSocialAccount[T <: AuthInfo](provider: String, profile: CommonSocialProfile, authInfo: T): Future[User] = {
    profile.email match {
      case Some(email) =>
        loginInfoDAO.getAuthenticationProviders(email).flatMap { providers =>
          if (providers.contains(provider) || providers.isEmpty) {
            for {
              user <- userService.createOrUpdate(
                loginInfo = profile.loginInfo,
                email = email,
                name = profile.fullName,
                providedHandle = None,
                avatarURL = profile.avatarURL)
              _ <- addAuthenticateMethod(user.userID, profile.loginInfo, authInfo)
            } yield user
          } else {
            Future.failed(EmailIsBeingUsed(providers))
          }
        }
      case None => Future.failed(NoEmailProvided)
    }
  }
}

case class EmailIsBeingUsed(providers: Seq[String]) extends Exception
case object NoEmailProvided extends Exception