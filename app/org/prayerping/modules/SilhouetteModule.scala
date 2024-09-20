package org.prayerping.modules

import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule
import org.prayerping.controllers.{ DefaultRememberMeConfig, DefaultSilhouetteControllerComponents, RememberMeConfig, SilhouetteControllerComponents }
import org.prayerping.models.daos.{ GoogleTotpInfoDAO, LoginInfoDAO, LoginInfoDAOImpl, OAuth2InfoDAO, PasswordInfoDAO, UserDAO, UserDAOImpl }
import org.prayerping.services.{ UserService, UserServiceImpl }
import org.prayerping.utils.auth.{ CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv }
import play.api.Configuration
import play.api.libs.openid.OpenIdClient
import play.api.libs.ws.WSClient
import play.api.mvc.{ Cookie, CookieHeaderEncoding }
import play.silhouette.api.actions.{ SecuredErrorHandler, UnsecuredErrorHandler }
import play.silhouette.api.crypto._
import play.silhouette.api.repositories.AuthInfoRepository
import play.silhouette.api.services._
import play.silhouette.api.util._
import play.silhouette.api.{ Environment, EventBus, Silhouette, SilhouetteProvider }
import play.silhouette.crypto.{ JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings }
import play.silhouette.impl.authenticators._
import play.silhouette.impl.providers._
import play.silhouette.impl.providers.oauth1._
import play.silhouette.impl.providers.oauth1.secrets.{ CookieSecretProvider, CookieSecretSettings }
import play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import play.silhouette.impl.providers.oauth2._
import play.silhouette.impl.providers.openid.YahooProvider
import play.silhouette.impl.providers.openid.services.PlayOpenIDService
import play.silhouette.impl.providers.state.{ CsrfStateItemHandler, CsrfStateSettings }
import play.silhouette.impl.services._
import play.silhouette.impl.util._
import play.silhouette.password.{ BCryptPasswordHasher, BCryptSha256PasswordHasher }
import play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.silhouette.persistence.repositories.DelegableAuthInfoRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
   * A very nested optional reader, to support these cases:
   * Not set, set None, will use default ('Lax')
   * Set to null, set Some(None), will use 'No Restriction'
   * Set to a string value try to match, Some(Option(string))
   */
  implicit val sameSiteReader: ValueReader[Option[Option[Cookie.SameSite]]] =
    (config: Config, path: String) => {
      if (config.hasPathOrNull(path)) {
        if (config.getIsNull(path))
          Some(None)
        else {
          Some(Cookie.SameSite.parse(config.getString(path)))
        }
      } else {
        None
      }
    }

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]
    bind[UserService].to[UserServiceImpl]
    bind[UserDAO].to[UserDAOImpl]
    bind[LoginInfoDAO].to[LoginInfoDAOImpl]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())

    bind[DelegableAuthInfoDAO[GoogleTotpInfo]].to[GoogleTotpInfoDAO]
    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAO]
    bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDAO]
  }

  /**
   * Provides the HTTP layer implementation.
   *
   * @param client Play's WS client.
   * @return The HTTP layer implementation.
   */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
   * Provides the Silhouette environment.
   *
   * @param userService The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
    userService: UserService,
    authenticatorService: AuthenticatorService[CookieAuthenticator],
    eventBus: EventBus): Environment[DefaultEnv] = {

    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
   * Provides the social provider registry.
   *
   * @param googleProvider The Google provider implementation.
   * @return The Silhouette environment.
   */
  @Provides
  def provideSocialProviderRegistry(
    googleProvider: GoogleProvider): SocialProviderRegistry = {

    SocialProviderRegistry(Seq(
      googleProvider
    ))
  }

  /**
   * Provides the signer for the OAuth1 token secret provider.
   *
   * @param configuration The Play configuration.
   * @return The signer for the OAuth1 token secret provider.
   */
  @Provides @Named("oauth1-token-secret-signer")
  def provideOAuth1TokenSecretSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.oauth1TokenSecretProvider.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the crypter for the OAuth1 token secret provider.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the OAuth1 token secret provider.
   */
  @Provides @Named("oauth1-token-secret-crypter")
  def provideOAuth1TokenSecretCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")

    new JcaCrypter(config)
  }

  /**
   * Provides the signer for the CSRF state item handler.
   *
   * @param configuration The Play configuration.
   * @return The signer for the CSRF state item handler.
   */
  @Provides @Named("csrf-state-item-signer")
  def provideCSRFStateItemSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.csrfStateItemHandler.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the signer for the social state handler.
   *
   * @param configuration The Play configuration.
   * @return The signer for the social state handler.
   */
  @Provides @Named("social-state-signer")
  def provideSocialStateSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.socialStateHandler.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the signer for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The signer for the authenticator.
   */
  @Provides @Named("authenticator-signer")
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.authenticator.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the crypter for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the authenticator.
   */
  @Provides @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  /**
   * Provides the auth info repository.
   *
   * @param totpInfoDAO     The implementation of the delegable totp auth info DAO.
   * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
   * @param oauth2InfoDAO   The implementation of the delegable OAuth2 auth info DAO.
   * @return The auth info repository instance.
   */
  @Provides
  def provideAuthInfoRepository(
    totpInfoDAO: DelegableAuthInfoDAO[GoogleTotpInfo],
    passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
    oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]): AuthInfoRepository = {

    new DelegableAuthInfoRepository(totpInfoDAO, passwordInfoDAO, oauth2InfoDAO)
  }

  /**
   * Provides the authenticator service.
   *
   * @param signer The signer implementation.
   * @param crypter The crypter implementation.
   * @param cookieHeaderEncoding Logic for encoding and decoding `Cookie` and `Set-Cookie` headers.
   * @param fingerprintGenerator The fingerprint generator implementation.
   * @param idGenerator The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    @Named("authenticator-signer") signer: Signer,
    @Named("authenticator-crypter") crypter: Crypter,
    cookieHeaderEncoding: CookieHeaderEncoding,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock): AuthenticatorService[CookieAuthenticator] = {

    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(config, None, signer, cookieHeaderEncoding, authenticatorEncoder, fingerprintGenerator, idGenerator, clock)
  }

  /**
   * Provides the avatar service.
   *
   * @param httpLayer The HTTP layer implementation.
   * @return The avatar service implementation.
   */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)

  /**
   * Provides the OAuth1 token secret provider.
   *
   * @param signer The signer implementation.
   * @param crypter The crypter implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @return The OAuth1 token secret provider implementation.
   */
  @Provides
  def provideOAuth1TokenSecretProvider(
    @Named("oauth1-token-secret-signer") signer: Signer,
    @Named("oauth1-token-secret-crypter") crypter: Crypter,
    configuration: Configuration,
    clock: Clock): OAuth1TokenSecretProvider = {

    val settings = configuration.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
    new CookieSecretProvider(settings, signer, crypter, clock)
  }

  /**
   * Provides the CSRF state item handler.
   *
   * @param idGenerator The ID generator implementation.
   * @param signer The signer implementation.
   * @param configuration The Play configuration.
   * @return The CSRF state item implementation.
   */
  @Provides
  def provideCsrfStateItemHandler(
    idGenerator: IDGenerator,
    @Named("csrf-state-item-signer") signer: Signer,
    configuration: Configuration): CsrfStateItemHandler = {
    val settings = configuration.underlying.as[CsrfStateSettings]("silhouette.csrfStateItemHandler")
    new CsrfStateItemHandler(settings, idGenerator, signer)
  }

  /**
   * Provides the social state handler.
   *
   * @param signer The signer implementation.
   * @return The social state handler implementation.
   */
  @Provides
  def provideSocialStateHandler(
    @Named("social-state-signer") signer: Signer,
    csrfStateItemHandler: CsrfStateItemHandler): SocialStateHandler = {

    new DefaultSocialStateHandler(Set(csrfStateItemHandler), signer)
  }

  /**
   * Provides the password hasher registry.
   *
   * @return The password hasher registry.
   */
  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

  /**
   * Provides the credentials provider.
   *
   * @param authInfoRepository The auth info repository implementation.
   * @param passwordHasherRegistry The password hasher registry.
   * @return The credentials provider.
   */
  @Provides
  def provideCredentialsProvider(
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }

  /**
   * Provides the TOTP provider.
   *
   * @return The credentials provider.
   */
  @Provides
  def provideTotpProvider(passwordHasherRegistry: PasswordHasherRegistry): GoogleTotpProvider = {
    new GoogleTotpProvider(passwordHasherRegistry)
  }

  /**
   * Provides the Google provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param socialStateHandler The social state handler implementation.
   * @param configuration The Play configuration.
   * @return The Google provider.
   */
  @Provides
  def provideGoogleProvider(
    httpLayer: HTTPLayer,
    socialStateHandler: SocialStateHandler,
    configuration: Configuration): GoogleProvider = {

    new GoogleProvider(httpLayer, socialStateHandler, configuration.underlying.as[OAuth2Settings]("silhouette.google"))
  }

  /**
   * Provides the remember me configuration.
   *
   * @param configuration The Play configuration.
   * @return The remember me config.
   */
  @Provides
  def providesRememberMeConfig(configuration: Configuration): RememberMeConfig = {
    val c = configuration.underlying
    DefaultRememberMeConfig(
      expiry = c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
      idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
      cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
    )
  }

  @Provides
  def providesSilhouetteComponents(components: DefaultSilhouetteControllerComponents): SilhouetteControllerComponents = {
    components
  }
}
