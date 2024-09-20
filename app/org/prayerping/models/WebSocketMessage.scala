package org.prayerping.models

import play.api.libs.json._

import java.util.UUID

sealed trait WebSocketMessage
case class NewPrayerRequest(request: PrayerRequest) extends WebSocketMessage
case class UpdatePrayerRequest(request: PrayerRequest) extends WebSocketMessage
case class DeletePrayerRequest(requestId: UUID) extends WebSocketMessage
case class NewPrayerRequestReaction(reaction: PrayerRequestReaction) extends WebSocketMessage
case class DeletePrayerRequestReaction(reaction: PrayerRequestReaction) extends WebSocketMessage
case class NewPrayerResponse(response: PrayerResponse) extends WebSocketMessage
case class UpdatePrayerResponse(response: PrayerResponse) extends WebSocketMessage
case class DeletePrayerResponse(response: PrayerResponse) extends WebSocketMessage
case class NewPrayerResponseReaction(reaction: PrayerResponseReaction) extends WebSocketMessage
case class DeletePrayerResponseReaction(reaction: PrayerResponseReaction) extends WebSocketMessage

object WebSocketMessage {
  implicit val webSocketMessageFormat: Format[WebSocketMessage] = new Format[WebSocketMessage] {
    def reads(json: JsValue): JsResult[WebSocketMessage] = {
      (json \ "type").validate[String].flatMap {
        case "newPrayerRequest" => (json \ "request").validate[PrayerRequest].map(NewPrayerRequest)
        case "updatePrayerRequest" => (json \ "request").validate[PrayerRequest].map(UpdatePrayerRequest)
        case "deletePrayerRequest" => (json \ "requestId").validate[UUID].map(DeletePrayerRequest)
        case "newPrayerRequestReaction" => (json \ "reaction").validate[PrayerRequestReaction].map(NewPrayerRequestReaction)
        case "deletePrayerRequestReaction" => (json \ "reaction").validate[PrayerRequestReaction].map(DeletePrayerRequestReaction)
        case "newPrayerResponse" => (json \ "response").validate[PrayerResponse].map(NewPrayerResponse)
        case "updatePrayerResponse" => (json \ "response").validate[PrayerResponse].map(UpdatePrayerResponse)
        case "deletePrayerResponse" => (json \ "response").validate[PrayerResponse].map(DeletePrayerResponse)
        case "newPrayerResponseReaction" => (json \ "reaction").validate[PrayerResponseReaction].map(NewPrayerResponseReaction)
        case "deletePrayerResponseReaction" => (json \ "reaction").validate[PrayerResponseReaction].map(DeletePrayerResponseReaction)
        case other => JsError(s"Unknown type: $other")
      }
    }

    def writes(o: WebSocketMessage): JsValue = o match {
      case NewPrayerRequest(request) => Json.obj(
        "type" -> "newPrayerRequest",
        "request" -> Json.toJson(request)
      )
      case UpdatePrayerRequest(request) => Json.obj(
        "type" -> "updatePrayerRequest",
        "request" -> Json.toJson(request)
      )
      case DeletePrayerRequest(requestId) => Json.obj(
        "type" -> "deletePrayerRequest",
        "requestId" -> requestId
      )
      case NewPrayerRequestReaction(reaction) => Json.obj(
        "type" -> "newPrayerRequestReaction",
        "reaction" -> Json.toJson(reaction)
      )
      case DeletePrayerRequestReaction(reaction) => Json.obj(
        "type" -> "deletePrayerRequestReaction",
        "reaction" -> Json.toJson(reaction)
      )
      case NewPrayerResponse(response) => Json.obj(
        "type" -> "newPrayerResponse",
        "response" -> Json.toJson(response)
      )
      case UpdatePrayerResponse(response) => Json.obj(
        "type" -> "updatePrayerResponse",
        "response" -> Json.toJson(response)
      )
      case DeletePrayerResponse(response) => Json.obj(
        "type" -> "deletePrayerResponse",
        "response" -> Json.toJson(response)
      )
      case NewPrayerResponseReaction(reaction) => Json.obj(
        "type" -> "newPrayerResponseReaction",
        "reaction" -> Json.toJson(reaction)
      )
      case DeletePrayerResponseReaction(reaction) => Json.obj(
        "type" -> "deletePrayerResponseReaction",
        "reaction" -> Json.toJson(reaction)
      )
    }
  }
}