package org.prayerping.jobs

import org.apache.pekko.actor.{ ActorRef, ActorSystem }
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.pekko.extension.quartz.QuartzSchedulerExtension

/**
 * Schedules the jobs.
 */
class Scheduler @Inject() (
  system: ActorSystem,
  @Named("auth-token-cleaner") authTokenCleaner: ActorRef) {

  QuartzSchedulerExtension(system).schedule("AuthTokenCleaner", authTokenCleaner, AuthTokenCleaner.Clean)

  authTokenCleaner ! AuthTokenCleaner.Clean
}
