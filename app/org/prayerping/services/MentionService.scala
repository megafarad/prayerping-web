package org.prayerping.services

import java.time.Instant
import java.util.UUID

trait MentionService {

  /**
   * Parses mentions from the raw input.
   *
   * @param mentions The set of mentions as raw strings.
   * @return A set of parsed mentions in the format "@handle@domain" or "@handle".
   */
  def parseMentions(mentions: Set[String]): Set[(String, Option[String])]

  /**
   * Determines which mentions to add and which to remove based on the current state.
   *
   * @param newMentions The new mentions provided in the update.
   * @param existingMentions The existing mentions stored in the database.
   * @return A tuple (mentionsToAdd, mentionsToRemove)
   */
  def determineMentionsToAddAndRemove(
    newMentions: Set[(String, Option[String])],
    existingMentions: Set[(String, Option[String])]): (Set[(String, Option[String])], Set[(String, Option[String])])

  /**
   * Creates a list of row tuples to add to the database.
   *
   * @param mentionsToAdd Mentions to add in raw format.
   * @param prayerRequestId The ID of the prayer request.
   * @return A sequence of tuples (id, prayerRequestId, handle, domain, whenCreated) ready for insertion.
   */
  def createMentionRowsToAdd(
    mentionsToAdd: Set[(String, Option[String])],
    prayerRequestId: UUID): Seq[(UUID, UUID, String, Option[String], Instant)]
}
