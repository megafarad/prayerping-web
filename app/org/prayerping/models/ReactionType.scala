package org.prayerping.models

import play.api.data._
import play.api.data.format.Formatter
import play.api.libs.json._

object ReactionType extends Enumeration {
  type ReactionType = Value
  val Pray: ReactionType.Value = Value("pray")
  val Peace: ReactionType.Value = Value("peace")
  val Hope: ReactionType.Value = Value("hope")
  val Love: ReactionType.Value = Value("love")
  val Thanks: ReactionType.Value = Value("thanks")

  implicit val reactionTypeJSFormat: Format[ReactionType] = new Format[ReactionType] {
    def reads(json: JsValue): JsResult[ReactionType.Value] = json match {
      case JsString(s) =>
        try {
          JsSuccess(ReactionType.withName(s))
        } catch {
          case _: NoSuchElementException => JsError(s"Unknown reaction type: $s")
        }
      case _ => JsError("String value expected")
    }

    def writes(reactionType: ReactionType): JsValue = JsString(reactionType.toString)
  }

  implicit val reactionTypeFormat: Formatter[ReactionType] = new Formatter[ReactionType] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ReactionType] = {
      data.get(key).map { value =>
        try {
          Right(ReactionType.withName(value))
        } catch {
          case _: NoSuchElementException => Left(Seq(FormError(key, s"Invalid value: $value")))
        }
      }.getOrElse(Left(Seq(FormError(key, "No value provided"))))
    }

    override def unbind(key: String, value: ReactionType): Map[String, String] = {
      Map(key -> value.toString)
    }
  }
}
