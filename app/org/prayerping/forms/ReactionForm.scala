package org.prayerping.forms

import org.prayerping.models.ReactionType
import play.api.data.Form
import play.api.data.Forms._

object ReactionForm {

  val form: Form[Data] = Form(
    mapping("reactionType" -> of[ReactionType.Value])(Data.apply)(Data.unapply)
  )

  case class Data(reactionType: ReactionType.Value)
}
