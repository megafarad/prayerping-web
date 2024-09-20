package org.prayerping.models

import play.api.libs.json.{ Format, Json }
import java.time.Instant
import java.util.UUID

case class PrayerGroupProfile(id: UUID, name: String, handle: String, domain: Option[String], description: String,
  whenCreated: Instant, whoCreated: PublicUserProfile, publicKey: String) extends Profile

object PrayerGroupProfile {
  implicit val prayerGroupProfileFormat: Format[PrayerGroupProfile] = Json.format[PrayerGroupProfile]
  implicit val prayerGroupProfilePageFormat: Format[Page[PrayerGroupProfile]] = Json.format[Page[PrayerGroupProfile]]
}
