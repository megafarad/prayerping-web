package org.prayerping.models

import play.api.libs.json.{ Format, Json }

import java.time.Instant
import java.util.UUID

case class PrayerRequest(id: UUID, user: PublicUserProfile, request: String, isAnonymous: Boolean, whenCreated: Instant,
  visibility: Visibility.Value, mentions: Set[String], canEdit: Boolean)

object PrayerRequest {
  implicit val prayerRequestFormat: Format[PrayerRequest] = Json.format[PrayerRequest]
  implicit val prayerRequestPageFormat: Format[Page[PrayerRequest]] = Json.format[Page[PrayerRequest]]
}