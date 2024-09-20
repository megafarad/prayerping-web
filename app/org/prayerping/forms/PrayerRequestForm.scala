package org.prayerping.forms

import org.prayerping.models.Visibility
import play.api.data.Form
import play.api.data.Forms._

object PrayerRequestForm {

  val form: Form[Data] = Form(
    mapping(
      "request" -> nonEmptyText,
      "isAnonymous" -> boolean,
      "visibility" -> of[Visibility.Value])(Data.apply)(Data.unapply))

  case class Data(request: String, isAnonymous: Boolean, visibility: Visibility.Value)
}
