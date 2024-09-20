package org.prayerping.actor

import org.apache.pekko.actor.{ Actor, ActorRef, Props }
import play.api.Configuration
import redis.api.pubsub._

import java.net.InetSocketAddress
import java.util.UUID
import javax.inject.Inject

class MessageDispatcherActor @Inject() (config: Configuration) extends Actor {
  import MessageDispatcherActor._

  private var subscriptions = Map.empty[String, Set[ActorRef]]

  private def getOrCreateSubscriberActorRef = context.child("subscriberActor").getOrElse {
    val redisConfig: Configuration = config.get[Configuration]("redis")
    val inetSocketAddress = new InetSocketAddress(
      redisConfig.get[String]("host"),
      redisConfig.get[Int]("port")
    )
    context.actorOf(SubscriberActor.props(inetSocketAddress, Nil, Nil), "subscriberActor")
  }

  def receive: Receive = {
    case Subscribe(channel, webSocketOut, userId) =>
      val updatedActorRefs = subscriptions.getOrElse(channel, Set.empty) + webSocketOut
      subscriptions += channel -> updatedActorRefs
      getOrCreateSubscriberActorRef ! SUBSCRIBE(channel)
    case Unsubscribe(channel, webSocketOut, userId) =>
      val updatedSet = subscriptions.getOrElse(channel, Set.empty) - webSocketOut
      if (updatedSet.isEmpty) {
        subscriptions -= channel
        getOrCreateSubscriberActorRef ! UNSUBSCRIBE(channel)
      } else subscriptions += channel -> updatedSet
    case RedisMessage(channel, message) =>
      // Forward the message to all subscribers of this channel
      subscriptions.getOrElse(channel, Set.empty).foreach(_ ! message)
  }
}

object MessageDispatcherActor {
  def props(config: Configuration): Props = Props(new MessageDispatcherActor(config))

  sealed trait MessageDispatcherActorMessage
  case class Subscribe(channel: String, webSocketOut: ActorRef, userId: UUID) extends MessageDispatcherActorMessage
  case class Unsubscribe(channel: String, webSocketOut: ActorRef, userId: UUID) extends MessageDispatcherActorMessage
  case class RedisMessage(channel: String, message: String) extends MessageDispatcherActorMessage
}
