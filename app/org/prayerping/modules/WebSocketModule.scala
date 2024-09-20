package org.prayerping.modules

import com.google.inject.AbstractModule
import org.prayerping.actor.MessageDispatcherActor
import play.api.libs.concurrent.PekkoGuiceSupport

class WebSocketModule extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bindActor[MessageDispatcherActor]("messageDispatcherActor")
  }
}
