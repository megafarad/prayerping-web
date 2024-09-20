package org.prayerping.controllers

import org.prayerping.services.HandleService
import org.prayerping.utils._
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class HandleController @Inject() (
  components: SilhouetteControllerComponents,
  handleService: HandleService)(implicit ec: ExecutionContext) extends SilhouetteController(components) {

  def getHandle(fullHandle: String): Action[AnyContent] = Action.async { _ =>
    fullHandle match {
      case handleRegex(localPart, domainPart) =>
        handleService.getHandle(localPart, Option(domainPart)) map {
          case Some(foundHandle) => Ok(Json.toJson(foundHandle))
          case None => NotFound
        }
      case _ => Future.successful(BadRequest)
    }
  }

  def getHandleSuggestions(query: String): Action[AnyContent] = Action.async { _ =>
    handleService.getHandleSuggestions(query) map {
      suggestions => Ok(Json.toJson(suggestions))
    }
  }

}
