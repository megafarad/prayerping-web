package org.prayerping.models

import play.api.libs.json.{ Format, Json }

import java.time.Instant
import java.util.UUID

case class PrayerResponse(id: UUID, requestId: UUID, user: PublicUserProfile, response: String, whenCreated: Instant,
  canEdit: Boolean)

object PrayerResponse {
  implicit val prayerResponseFormat: Format[PrayerResponse] = Json.format[PrayerResponse]
  implicit val prayerResponsePageFormat: Format[Page[PrayerResponse]] = Json.format[Page[PrayerResponse]]
}