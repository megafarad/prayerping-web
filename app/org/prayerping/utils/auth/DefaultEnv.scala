package org.prayerping.utils.auth

import play.silhouette.api.Env
import play.silhouette.impl.authenticators.CookieAuthenticator
import org.prayerping.models.User

/**
 * The default env.
 */
trait DefaultEnv extends Env {
  type I = User
  type A = CookieAuthenticator
}
