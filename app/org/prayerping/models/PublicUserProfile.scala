package org.prayerping.models

import play.api.libs.json._

import java.time.Instant
import java.util.UUID

/**
 * A public user profile - user without an email or private key
 *
 * @param userID         The unique ID of the user.
 * @param handle         The user's handle.
 * @param domain         The user's domain.
 * @param name           A name for display.
 * @param faithTradition Maybe the user's faith tradition.
 * @param avatarURL      Maybe the avatar URL of the authenticated provider.
 * @param profile        Maybe the profile of the authenticated user.
 * @param signedUpAt     The date/time when the user signed up.
 * @param publicKey      The user's public key
 */
case class PublicUserProfile(
  userID: UUID,
  handle: String,
  domain: Option[String],
  name: Option[String],
  faithTradition: Option[String],
  avatarURL: Option[String],
  profile: Option[String],
  signedUpAt: Instant,
  publicKey: String) extends Profile

object PublicUserProfile {
  implicit val publicUserFormat: Format[PublicUserProfile] = Json.format[PublicUserProfile]
}