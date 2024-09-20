package org.prayerping.models

import play.api.libs.json.{ Format, Json, OFormat }

import java.util.UUID

case class PrayerResponseReaction(id: UUID, responseId: UUID, user: PublicUserProfile, reactionType: ReactionType.Value)

object PrayerResponseReaction {
  implicit val prayerResponseReactionFormat: Format[PrayerResponseReaction] = Json.format[PrayerResponseReaction]
}