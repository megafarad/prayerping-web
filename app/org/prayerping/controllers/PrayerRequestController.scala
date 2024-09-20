package org.prayerping.controllers

import org.prayerping.forms.PrayerRequestForm
import org.prayerping.models.User
import org.prayerping.services.{ PrayerRequestService, SearchService }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PrayerRequestController @Inject() (
  components: SilhouetteControllerComponents,
  prayerRequestService: PrayerRequestService,
  searchService: SearchService)(implicit ec: ExecutionContext) extends SilhouetteController(components) {

  def getPrayerRequests(query: Option[String], page: Int, pageSize: Int): Action[AnyContent] = UserAwareAction.async {
    request =>
      val requester: Option[User] = request.identity
      val requestsPageFuture = query match {
        case Some(q) => searchService.searchPrayerRequests(q, page, pageSize, requester.map(_.userID))
        case None => prayerRequestService.getPrayerRequests(page, pageSize, requester.map(_.userID))
      }
      requestsPageFuture map {
        requestsPage => Ok(Json.toJson(requestsPage))
      }
  }

  def createPrayerRequest: Action[AnyContent] = SecuredAction.async { implicit request =>
    PrayerRequestForm.form.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      data =>
        prayerRequestService.createPrayerRequest(request.identity.userID, data.request, data.isAnonymous, data.visibility) map {
          prayerRequest => Ok(Json.toJson(prayerRequest))
        }
    )
  }

  def getPrayerRequest(id: UUID): Action[AnyContent] = UserAwareAction.async { request =>
    prayerRequestService.getPrayerRequest(id, request.identity.map(_.userID)) map {
      case Some(prayerRequest) => Ok(Json.toJson(prayerRequest))
      case None => NotFound
    }
  }

  def updatePrayerRequest(id: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    prayerRequestService.getPrayerRequest(id, Some(request.identity.userID)) flatMap {
      case None => Future.successful(NotFound)
      case Some(prayerRequest) => if (prayerRequest.canEdit) {
        PrayerRequestForm.form.bindFromRequest().fold(
          _ => Future.successful(BadRequest),
          data => prayerRequestService.updatePrayerRequest(id, data.request, data.isAnonymous, data.visibility,
            Some(request.identity.userID)) map {
              prayerRequest => Ok(Json.toJson(prayerRequest))
            }
        )
      } else Future.successful(Forbidden)
    }
  }

  def deletePrayerRequest(id: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    prayerRequestService.getPrayerRequest(id, Some(request.identity.userID)) flatMap {
      case None => Future.successful(NotFound)
      case Some(prayerRequest) => if (prayerRequest.canEdit) {
        prayerRequestService.deletePrayerRequest(prayerRequest) map {
          _ => NoContent
        }
      } else Future.successful(Forbidden)
    }
  }
}
