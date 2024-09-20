package org.prayerping.forms

import play.api.data.Form
import play.api.data.Forms._

object PrayerGroupForm {

  private def validateHandle(handle: String): Boolean = {
    handle.matches("^[A-Za-z0-9]*$")
  }

  val form: Form[Data] = Form(mapping(
    "name" -> nonEmptyText,
    "handle" -> nonEmptyText.verifying(handle => validateHandle(handle)),
    "description" -> nonEmptyText)(Data.apply)(Data.unapply))

  case class Data(name: String, handle: String, description: String)
}
