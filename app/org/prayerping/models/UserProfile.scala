package org.prayerping.models

import play.api.libs.json._

import java.time.Instant
import java.util.UUID

/**
 * A user profile - the user without a private key.
 *
 * @param userID          The unique ID of the user.
 * @param handle          The user's handle.
 * @param domain          The user's domain.
 * @param name            A name for display.
 * @param faithTradition  Maybe the user's faith tradition.
 * @param email           Maybe the email of the authenticated provider.
 * @param avatarURL       Maybe the avatar URL of the authenticated provider.
 * @param profile         Maybe the profile of the authenticated user.
 * @param signedUpAt      The date/time when the user signed up.
 * @param activated       Indicates that the user has activated its registration.
 * @param publicKey       The user's public key
 */
case class UserProfile(
  userID: UUID,
  handle: String,
  domain: Option[String],
  name: Option[String],
  faithTradition: Option[String],
  email: Option[String],
  avatarURL: Option[String],
  profile: Option[String],
  signedUpAt: Instant,
  activated: Boolean,
  publicKey: String)

object UserProfile {
  implicit val userProfileFormat: Format[UserProfile] = Json.format[UserProfile]
}
