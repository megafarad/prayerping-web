package org.prayerping.models

import play.api.libs.json.{ Format, Json }

import java.util.UUID

case class PrayerRequestGroup(requestId: UUID, groupId: UUID)

object PrayerRequestGroup {
  implicit val prayerRequestGroupFormat: Format[PrayerRequestGroup] = Json.format[PrayerRequestGroup]
}