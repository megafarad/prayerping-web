package org.prayerping.models

import java.time.Instant
import java.util.UUID

case class PrayerGroup(id: UUID, name: String, handle: String, domain: Option[String], description: String,
  whenCreated: Instant, whoCreated: PublicUserProfile, publicKey: String,
  privateKey: Option[String], salt: Option[Array[Byte]]) {
  def getProfile: PrayerGroupProfile = PrayerGroupProfile(id, name, handle, domain, description, whenCreated,
    whoCreated, publicKey)
}