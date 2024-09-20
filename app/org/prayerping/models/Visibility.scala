package org.prayerping.models

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json._

object Visibility extends Enumeration {
  type Visibility = Value
  val Public: Visibility.Value = Value("public")
  val Unlisted: Visibility.Value = Value("unlisted")
  val Private: Visibility.Value = Value("private")
  val Direct: Visibility.Value = Value("direct")

  implicit val visibilityJSFormat: Format[Visibility] = new Format[Visibility] {
    def reads(json: JsValue): JsResult[Visibility] = json match {
      case JsString(s) =>
        try {
          JsSuccess(Visibility.withName(s))
        } catch {
          case _: NoSuchElementException => JsError(s"Unknown visibility: $s")
        }
      case _ => JsError("String value expected")
    }

    def writes(o: Visibility): JsValue = JsString(o.toString)
  }

  implicit val visibilityFormat: Formatter[Visibility] = new Formatter[Visibility] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Visibility] = {
      data.get(key).map { value =>
        try {
          Right(Visibility.withName(value))
        } catch {
          case _: NoSuchElementException => Left(Seq(FormError(key, s"Invalid value: $value")))
        }
      }.getOrElse(Left(Seq(FormError(key, "No value provided"))))
    }

    def unbind(key: String, value: Visibility): Map[String, String] = {
      Map(key -> value.toString)
    }
  }
}
