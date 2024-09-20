package org.prayerping.models

import play.api.libs.json._

sealed trait WebSocketCommand
case class Subscribe(channel: String) extends WebSocketCommand
case class Unsubscribe(channel: String) extends WebSocketCommand

object WebSocketCommand {
  implicit val webSocketCommandFormat: Format[WebSocketCommand] = new Format[WebSocketCommand] {
    def reads(json: JsValue): JsResult[WebSocketCommand] = {
      (json \ "type").validate[String].flatMap {
        case "subscribe" => (json \ "channel").validate[String].map(Subscribe)
        case "unsubscribe" => (json \ "channel").validate[String].map(Unsubscribe)
        case other => JsError(s"Unknown type: $other")
      }
    }

    def writes(o: WebSocketCommand): JsValue = o match {
      case Subscribe(channel) => Json.obj(
        "type" -> "subscribe",
        "channel" -> channel
      )
      case Unsubscribe(channel) => Json.obj(
        "type" -> "unsubscribe",
        "channel" -> channel
      )
    }
  }
}