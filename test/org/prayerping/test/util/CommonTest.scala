package org.prayerping.test.util

import org.prayerping.models.User

import java.time.Instant
import java.util.UUID

trait CommonTest {

  val testUser: User = User(
    userID = UUID.randomUUID(),
    handle = "testHandle",
    domain = None,
    name = Some("test name"),
    faithTradition = Some("religion"),
    email = Some("email@example.com"),
    avatarURL = None,
    profile = None,
    signedUpAt = Instant.now(),
    activated = true,
    publicKey = "FAKE PUBLIC KEY",
    privateKey = None,
    salt = None
  )
}
