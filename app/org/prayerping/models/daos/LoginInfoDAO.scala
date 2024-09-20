package org.prayerping.models.daos

import org.prayerping.models.User
import play.silhouette.api.LoginInfo

import java.util.UUID
import scala.concurrent.Future

trait LoginInfoDAO {
  /**
   * Get list of user authentication methods providers
   *
   * @param email user email
   * @return
   */
  def getAuthenticationProviders(email: String): Future[Seq[String]]

  /**
   * Finds a user and login info pair by userID and login info providerID
   *
   * @param userId     user id
   * @param providerId provider id
   * @return Some(User, LoginInfo) if there is a user by userId which has login method for provider by provider ID, otherwise None
   */
  def find(userId: UUID, providerId: String): Future[Option[(User, LoginInfo)]]

  /**
   * Saves a login info for user
   *
   * @param userID    The user id.
   * @param loginInfo login info
   * @return unit
   */
  def saveUserLoginInfo(userID: UUID, loginInfo: LoginInfo): Future[Unit]

  /**
   * Deletes login info for a user
   *
   * @param userID      The user ID
   * @param loginInfo   Login Info
   * @return
   */
  def deleteUserLoginInfo(userID: UUID, loginInfo: LoginInfo): Future[Unit]
}
