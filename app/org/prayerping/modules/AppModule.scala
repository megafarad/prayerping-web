package org.prayerping.modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import org.prayerping.models.daos._
import org.prayerping.services._
import org.prayerping.utils.config.AnonymousUserConfig

class AppModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[PrayerGroupDAO].to[PrayerGroupDAOImpl]
    bind[PrayerRequestDAO].to[PrayerRequestDAOImpl]
    bind[PrayerRequestReactionDAO].to[PrayerRequestReactionDAOImpl]
    bind[PrayerResponseDAO].to[PrayerResponseDAOImpl]
    bind[PrayerResponseReactionDAO].to[PrayerResponseReactionDAOImpl]
    bind[HandleDAO].to[HandleDAOImpl]
    bind[FollowDAO].to[FollowDAOImpl]
    bind[MentionService].to[MentionServiceImpl]
    bind[HandleService].to[HandleServiceImpl]
    bind[FollowService].to[FollowServiceImpl]
    bind[ReactionService].to[ReactionServiceImpl]
    bind[PrayerGroupService].to[PrayerGroupServiceImpl]
    bind[SearchService].to[SearchServiceImpl]
    bind[PrayerRequestService].to[PrayerRequestServiceImpl]
    bind[PrayerFeedService].to[PrayerFeedServiceImpl]
    bind[PrayerResponseService].to[PrayerResponseServiceImpl]
    bind(classOf[AnonymousUserConfig]).asEagerSingleton()
    bind(classOf[CryptoService]).asEagerSingleton()
  }
}
