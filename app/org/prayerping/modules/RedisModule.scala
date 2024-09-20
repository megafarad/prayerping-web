package org.prayerping.modules

import com.google.inject.AbstractModule
import org.prayerping.providers.RedisClientProvider

class RedisModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[RedisClientProvider]).asEagerSingleton()
  }

}
