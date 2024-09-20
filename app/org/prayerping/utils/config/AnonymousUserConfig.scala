package org.prayerping.utils.config

import org.prayerping.models.User
import org.prayerping.models.daos.{ HandleDAO, UserDAO }
import org.prayerping.services.CryptoService
import play.api.Configuration

import java.time.format.DateTimeFormatter
import java.time.{ Instant, ZoneId }
import java.util.UUID
import javax.inject._
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

@Singleton
class AnonymousUserConfig @Inject() (
  config: Configuration,
  userDAO: UserDAO,
  cryptoService: CryptoService)(implicit ec: ExecutionContext) {
  val userID: UUID = UUID.fromString(config.get[String]("prayerping.anonymous.user.userID"))
  val handle: String = config.get[String]("prayerping.anonymous.user.handle")
  val name: String = config.get[String]("prayerping.anonymous.user.name")
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
  val signedUpAt: Instant = Instant.from(
    dateTimeFormatter.parse(config.get[String]("prayerping.anonymous.user.signedUpAt")))

  private def getOrCreateAnonymousUser: Future[User] = userDAO.find(userID) flatMap {
    case Some(anonymousUser) => Future.successful(anonymousUser)
    case None =>
      val salt = cryptoService.generateSalt
      val (publicKey, encryptedPrivateKey) = cryptoService.generateKeyPair(salt)
      for {

        user <- userDAO.save(User(
          userID = userID,
          handle = handle,
          domain = None,
          name = Some(name),
          faithTradition = None,
          email = None,
          avatarURL = None,
          profile = None,
          signedUpAt = signedUpAt,
          activated = true,
          publicKey = publicKey,
          privateKey = Some(encryptedPrivateKey),
          salt = Some(salt)))

      } yield user
  }

  val anonymousUser: User = Await.result(getOrCreateAnonymousUser, Duration.Inf)

}
