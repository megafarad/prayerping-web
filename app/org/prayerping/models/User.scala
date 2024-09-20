package org.prayerping.models

import play.silhouette.api.Identity

import java.time.Instant
import java.util.UUID

/**
 * The user object.
 *
 * @param userID          The unique ID of the user.
 * @param handle          The user's handle.
 * @param domain          Maybe the user's domain.
 * @param name            A name for display.
 * @param faithTradition  Maybe the user's faith tradition.
 * @param email           Maybe the email of the authenticated provider.
 * @param avatarURL       Maybe the avatar URL of the authenticated provider.
 * @param profile         Maybe the profile of the authenticated user.
 * @param signedUpAt      The date/time when the user signed up.
 * @param activated       Indicates that the user has activated its registration.
 * @param publicKey       The user's public key.
 * @param privateKey      Maybe the user's private key.
 * @param salt            Maybe the user's salt.
 */
case class User(
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
  publicKey: String,
  privateKey: Option[String],
  salt: Option[Array[Byte]]) extends Identity {
  def getPublicUserProfile: PublicUserProfile = PublicUserProfile(userID, handle, domain, name, faithTradition, avatarURL,
    profile, signedUpAt, publicKey)
  def getUserProfile: UserProfile = UserProfile(userID, handle, domain, name, faithTradition, email, avatarURL, profile,
    signedUpAt, activated, publicKey)
}
