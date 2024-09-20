package org.prayerping.models

import play.api.libs.json.{ Format, Json }

import java.time.Instant
import java.util.UUID

case class PrayerGroupMembership(groupId: UUID, user: PublicUserProfile, whenCreated: Instant)

object PrayerGroupMembership {
  implicit val prayerGroupMembershipFormat: Format[PrayerGroupMembership] = Json.format[PrayerGroupMembership]
  implicit val prayerGroupMembershipPageFormat: Format[Page[PrayerGroupMembership]] =
    Json.format[Page[PrayerGroupMembership]]
}
