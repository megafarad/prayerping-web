package org.prayerping

import scala.util.matching.Regex

package object utils {
  val handleRegex: Regex = """@?([a-zA-Z0-9_]+)(?:@([a-zA-Z0-9.-]+\.[a-zA-Z]{2,}))?""".r
  val mentionRegex: Regex = """@([a-zA-Z0-9_]+)(?:@([a-zA-Z0-9.-]+\.[a-zA-Z]{2,}))?""".r

  private def findAllMentions(text: String) = {
    mentionRegex.findAllMatchIn(text).map { m =>
      val localPart = m.group(1)
      val domainPart = Option(m.group(2)) // Domain part may be None
      (localPart, domainPart)
    }
  }

  def parseMentions(text: String): Set[(String, Option[String])] = {
    findAllMentions(text).toSet
  }

  def parseMention(mention: String): Option[(String, Option[String])] = {
    findAllMentions(mention).toSeq.headOption
  }
}
