package org.prayerping.modules

import net.codingwell.scalaguice.ScalaModule
import org.prayerping.jobs.{ AuthTokenCleaner, Scheduler }
import play.api.libs.concurrent.PekkoGuiceSupport

/**
 * The job module.
 */
class JobModule extends ScalaModule with PekkoGuiceSupport {

  /**
   * Configures the module.
   */
  override def configure() = {
    bindActor[AuthTokenCleaner]("auth-token-cleaner")
    bind[Scheduler].asEagerSingleton()
  }
}
