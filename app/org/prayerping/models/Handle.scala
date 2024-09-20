package org.prayerping.models

import play.api.libs.json._
import PublicUserProfile._

sealed trait Handle
case class UserHandle(user: PublicUserProfile) extends Handle
case class GroupHandle(group: PrayerGroupProfile) extends Handle

object Handle {
  implicit val handleFormat: Format[Handle] = new Format[Handle] {
    override def writes(handle: Handle): JsValue = handle match {
      case UserHandle(user) => Json.obj(
        "type" -> "user",
        "user" -> Json.toJson(user)
      )
      case GroupHandle(group) => Json.obj(
        "type" -> "group",
        "group" -> Json.toJson(group)
      )
    }

    override def reads(json: JsValue): JsResult[Handle] = {
      (json \ "type").validate[String].flatMap {
        case "user" => (json \ "user").validate[PublicUserProfile].map(UserHandle)
        case "group" => (json \ "group").validate[PrayerGroupProfile].map(GroupHandle)
        case other => JsError(s"Unknown type: $other")
      }
    }
  }
}
