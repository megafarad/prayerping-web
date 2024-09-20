package org.prayerping.controllers

import org.prayerping.forms.PrayerResponseForm
import org.prayerping.services.{ PrayerResponseService, SearchService }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PrayerResponseController @Inject() (
  components: SilhouetteControllerComponents,
  prayerResponseService: PrayerResponseService,
  searchService: SearchService)(implicit ec: ExecutionContext) extends SilhouetteController(components) {
  def getPrayerResponses(requestId: UUID, query: Option[String], page: Int,
    pageSize: Int): Action[AnyContent] = UserAwareAction.async { request =>
    val requesterId = request.identity.map(_.userID)
    val responsePageFuture = query match {
      case Some(q) => searchService.searchPrayerResponses(requestId, q, page, pageSize, requesterId)
      case None => prayerResponseService.getPrayerResponses(requestId, page, pageSize, requesterId)
    }
    responsePageFuture map {
      responsePage => Ok(Json.toJson(responsePage))
    }
  }

  def createPrayerResponse(requestID: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    PrayerResponseForm.form.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      data => prayerResponseService.createPrayerResponse(requestID, request.identity.userID, data.response) map {
        prayerResponse => Ok(Json.toJson(prayerResponse))
      }
    )
  }

  def getPrayerResponse(responseId: UUID): Action[AnyContent] = UserAwareAction.async { request =>
    prayerResponseService.getPrayerResponse(responseId, request.identity.map(_.userID)) map {
      case None => NotFound
      case Some(prayerResponse) => Ok(Json.toJson(prayerResponse))
    }
  }

  def updatePrayerResponse(responseId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    prayerResponseService.getPrayerResponse(responseId, Some(request.identity.userID)) flatMap {
      case Some(prayerResponse) if prayerResponse.canEdit =>
        PrayerResponseForm.form.bindFromRequest().fold(
          _ => Future.successful(BadRequest),
          data => prayerResponseService.updatePrayerResponse(responseId, data.response, Some(request.identity.userID)) map {
            prayerResponse => Ok(Json.toJson(prayerResponse))
          }
        )
      case _ => Future.successful(NotFound)
    }
  }

  def deletePrayerResponse(responseId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    prayerResponseService.getPrayerResponse(responseId, Some(request.identity.userID)) flatMap {
      case None => Future.successful(NotFound)
      case Some(prayerResponse) => if (prayerResponse.user.userID == request.identity.userID) {
        prayerResponseService.deletePrayerResponse(prayerResponse).map(_ => NoContent)
      } else Future.successful(Forbidden)
    }
  }

}
