package org.prayerping.modules

import com.google.inject.{ AbstractModule, Provides }
import net.codingwell.scalaguice.ScalaModule
import org.prayerping.models.daos.{ AuthTokenDAO, AuthTokenDAOImpl }
import org.prayerping.models.daos.{ AuthTokenDAO, AuthTokenDAOImpl }
import org.prayerping.services.{ AuthTokenService, AuthTokenServiceImpl }
import org.prayerping.services.captcha.{ CaptchaService, ReCaptchaConfig, ReCaptchaService }
import play.api.Configuration

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[CaptchaService].to[ReCaptchaService]
  }

  @Provides
  def providesReCaptchaConfig(conf: Configuration): ReCaptchaConfig = {
    ReCaptchaConfig(conf.get[String]("recaptcha.secretKey"))
  }
}
