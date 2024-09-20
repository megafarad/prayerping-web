package org.prayerping.services

import com.fasterxml.uuid.Generators
import org.prayerping.models.User
import org.prayerping.models.daos.{ LoginInfoDAO, UserDAO }
import play.silhouette.api.LoginInfo

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 * @param ex      The execution context.
 */
class UserServiceImpl @Inject() (userDAO: UserDAO, loginInfoDAO: LoginInfoDAO, cryptoService: CryptoService)(implicit ex: ExecutionContext) extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[User]] = userDAO.find(id)

  /**
   * Retrieves a user and login info pair by userID and login info providerID
   *
   * @param id         The ID to retrieve a user.
   * @param providerID The ID of login info provider.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieveUserLoginInfo(id: UUID, providerID: String): Future[Option[(User, LoginInfo)]] =
    loginInfoDAO.find(id, providerID)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
   * Retrieve a user by its handle and domain.
   *
   * @param handle The handle of the user
   * @param domain The domain of the user
   * @return A user if found, or None if no user could be retrieved for the given handle/domain.
   */
  def retrieve(handle: String, domain: Option[String]): Future[Option[User]] = userDAO.findByHandle(handle, domain)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User] = userDAO.save(user)

  /**
   * Creates or updates a user
   *
   * If a user exists for given login info or email then update the user, otherwise create a new user with the given data
   *
   * @param loginInfo      social profile
   * @param email          user email
   * @param name           user name
   * @param providedHandle provided handle - method generates a random handle for user creation
   * @param avatarURL      avatar URL
   * @return
   */
  def createOrUpdate(
    loginInfo: LoginInfo,
    email: String,
    name: Option[String],
    providedHandle: Option[String],
    avatarURL: Option[String]): Future[User] = {
    Future.sequence(Seq(userDAO.find(loginInfo), userDAO.findByEmail(email))).flatMap { users =>
      users.flatten.headOption match {
        case Some(user) => userDAO.save(user.copy(
          name = name,
          email = Some(email),
          avatarURL = avatarURL
        ))
        case None =>
          val salt = cryptoService.generateSalt
          val (publicKey, encryptedPrivateKey) = cryptoService.generateKeyPair(salt)
          userDAO.save(User(
            userID = Generators.timeBasedReorderedGenerator().generate(),
            handle = providedHandle.getOrElse(generateHandle(name.getOrElse("u"))),
            domain = None,
            name = name,
            faithTradition = None,
            email = Some(email),
            avatarURL = avatarURL,
            profile = None,
            signedUpAt = Instant.now(),
            activated = false,
            publicKey = publicKey,
            privateKey = Some(encryptedPrivateKey),
            salt = Some(salt)))
      }
    }
  }

  private def generateHandle(name: String): String = {
    val namePortion = name.replaceAll("\\s+", "")
    val numberOfDigits = if (namePortion.length > 16) {
      6
    } else 16 - namePortion.length + 6
    val numberPortion = generateRandomDigits(numberOfDigits)
    namePortion + numberPortion
  }

  private def generateRandomDigits(length: Int): String = {
    require(length > 0, "Length must be greater than zero.")
    val random = new Random
    (1 to length).map(_ => random.nextInt(10)).mkString
  }

}
