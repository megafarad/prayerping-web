package org.prayerping.actor

import org.apache.pekko.actor.Props
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{ Message, PMessage }

import java.net.InetSocketAddress

class SubscriberActor(
  inetSocketAddress: InetSocketAddress,
  channels: Seq[String],
  patterns: Seq[String]) extends RedisSubscriberActor(
  inetSocketAddress, channels, patterns, onConnectStatus = _ => ()
) {

  def onMessage(m: Message): Unit =
    context.parent ! MessageDispatcherActor.RedisMessage(m.channel, m.data.utf8String)

  def onPMessage(pm: PMessage): Unit =
    context.parent ! MessageDispatcherActor.RedisMessage(pm.channel, pm.data.utf8String)
}

object SubscriberActor {
  def props(inetSocketAddress: InetSocketAddress, channels: Seq[String], patterns: Seq[String]): Props = Props(
    new SubscriberActor(inetSocketAddress, channels, patterns))
}