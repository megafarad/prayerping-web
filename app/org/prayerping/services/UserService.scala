package org.prayerping.services

import org.prayerping.models.User
import play.silhouette.api.LoginInfo
import play.silhouette.api.services.IdentityService

import java.util.UUID
import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[User]]

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]]

  /**
   * Retrieve a user by its handle and domain.
   *
   * @param handle  The handle of the user
   * @param domain  The domain of the user
   * @return A user if found, or None if no user could be retrieved for the given handle/domain.
   */
  def retrieve(handle: String, domain: Option[String]): Future[Option[User]]

  /**
   * Retrieves a user and login info pair by userID and login info providerID
   *
   * @param id         The ID to retrieve a user.
   * @param providerID The ID of login info provider.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieveUserLoginInfo(id: UUID, providerID: String): Future[Option[(User, LoginInfo)]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
   * Creates or updates a user
   *
   * If a user exists for given login info or email then update the user, otherwise create a new user with the given data
   *
   * @param loginInfo       social profile
   * @param email           user email
   * @param name            user name
   * @param providedHandle  provided handle - method generates a random handle for user creation
   * @param avatarURL       avatar URL
   * @return
   */
  def createOrUpdate(
    loginInfo: LoginInfo,
    email: String,
    name: Option[String],
    providedHandle: Option[String],
    avatarURL: Option[String]): Future[User]
}
