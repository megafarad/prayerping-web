package org.prayerping.models

object EntityType extends Enumeration {
  type EntityType = Value
  val User: EntityType.Value = Value("user")
  val Group: EntityType.Value = Value("group")
}
