package org.prayerping.models

trait Profile {
  val handle: String
  val domain: Option[String]

  val mention: String = domain match {
    case Some(value) => s"@$handle@$value"
    case None => s"@$handle"
  }
}
