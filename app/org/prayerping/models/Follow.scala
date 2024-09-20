package org.prayerping.models

import play.api.libs.json.{ Format, Json }

import java.time.Instant

case class Follow(user: PublicUserProfile, targetUser: PublicUserProfile, whenCreated: Instant)

object Follow {
  implicit val followFormat: Format[Follow] = Json.format[Follow]
}
