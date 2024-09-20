package org.prayerping.models

import play.api.libs.json.{ Format, Json }
import ReactionType._

import java.util.UUID

case class PrayerRequestReaction(id: UUID, requestId: UUID, user: PublicUserProfile, reactionType: ReactionType.Value)

object PrayerRequestReaction {
  implicit val prayerRequestReactionFormat: Format[PrayerRequestReaction] = Json.format[PrayerRequestReaction]
}
