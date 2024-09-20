package org.prayerping.forms

import play.api.data.Form
import play.api.data.Forms._

object PrayerResponseForm {

  val form: Form[Data] = Form(
    mapping("response" -> nonEmptyText)(Data.apply)(Data.unapply)
  )

  case class Data(response: String)
}
