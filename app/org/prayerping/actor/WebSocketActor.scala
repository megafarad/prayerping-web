package org.prayerping.actor

import org.apache.pekko.actor.{ Actor, ActorRef, Props }
import org.prayerping.models._
import play.api.Logging
import play.api.libs.json._

import java.util.UUID

object WebSocketActor {
  def props(out: ActorRef, dispatcher: ActorRef, userId: UUID): Props = Props(new WebSocketActor(out, dispatcher, userId))
}
class WebSocketActor(out: ActorRef, dispatcher: ActorRef, userId: UUID) extends Actor with Logging {

  def receive: Receive = {
    case msg: String =>
      val command = Json.fromJson[WebSocketCommand](Json.parse(msg))
      command match {
        case JsSuccess(value, _) => value match {
          case Subscribe(channel) =>
            dispatcher ! MessageDispatcherActor.Subscribe(channel, out, userId)
          case Unsubscribe(channel) =>
            dispatcher ! MessageDispatcherActor.Unsubscribe(channel, out, userId)
        }
        case JsError(errors) =>
          logger.error(s"Unable to parse WebSocketCommand: $errors")
      }
  }
}