package org.prayerping.forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the sign up process.
 */
object SignUpForm {

  private def validateHandle(handle: String): Boolean = {
    handle.matches("^[A-Za-z0-9]*$")
  }

  /**
   * A play framework form.
   */
  val form: Form[Data] = Form(
    mapping(
      "handle" -> nonEmptyText.verifying(handle => validateHandle(handle)),
      "name" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText,
      "faithTradition" -> optional(text),
      "profile" -> optional(text),
      "g-recaptcha-response" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
   *
   * @param handle
   * @param name
   * @param email
   * @param password
   * @param faithTradition
   * @param profile
   * @param captchaResponse
   */
  case class Data(
    handle: String,
    name: String,
    email: String,
    password: String,
    faithTradition: Option[String],
    profile: Option[String],
    captchaResponse: String)
}
