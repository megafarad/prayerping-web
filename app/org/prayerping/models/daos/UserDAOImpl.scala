package org.prayerping.models.daos

import org.prayerping.models.{ EntityType, User }
import org.prayerping.models.daos.PostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider
import play.silhouette.api.LoginInfo

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
/**
 * Give access to the user object.
 */
class UserDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends UserDAO with DAOSlick {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    val userQuery = for {
      loginInfoTable <- loginInfoQuery(loginInfo)
      userLoginInfoTable <- userLoginInfoTableQuery.filter(_.loginInfoID === loginInfoTable.id)
      user <- userTableQuery.filter(_.id === userLoginInfoTable.userID)
    } yield user
    db.run(userQuery.result.headOption)
  }

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID): Future[Option[User]] =
    db.run(userTableQuery.filter(_.id === userID).result.headOption)
  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User] =
    db.run(DBIO.seq(
      handleTableQuery.insertOrUpdate((user.handle, user.domain, EntityType.User, user.userID, user.signedUpAt)),
      userTableQuery.insertOrUpdate(user)
    ).transactionally).map(_ => user)

  /**
   * Finds a user by its email
   *
   * @param email The email of the user to find
   * @return The found user or None if no user can be found
   */
  def findByEmail(email: String): Future[Option[User]] = {
    db.run(userTableQuery.filter(_.email === email).result.headOption)
  }

  /**
   * Finds a user by its handle
   *
   * @param handle The handle of the user to find
   * @param domain The domain of the user to find
   * @return The found user or None if no user can be found
   *
   */
  def findByHandle(handle: String, domain: Option[String]): Future[Option[User]] =
    db.run(userTableQuery.filter(user => user.handle === handle && user.domain === domain).result.headOption)
}
