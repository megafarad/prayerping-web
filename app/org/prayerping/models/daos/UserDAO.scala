package org.prayerping.models.daos

import java.util.UUID
import play.silhouette.api.LoginInfo
import org.prayerping.models.User

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait UserDAO {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]]

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
   * Finds a user by its email
   * @param email The email of the user to find
   * @return The found user or None if no user can be found
   */
  def findByEmail(email: String): Future[Option[User]]

  /**
   * Finds a user by its handle
   *
   * @param handle The handle of the user to find
   * @param domain The domain of the user to find
   * @return The found user or None if no user can be found
   *
   */
  def findByHandle(handle: String, domain: Option[String]): Future[Option[User]]
}
