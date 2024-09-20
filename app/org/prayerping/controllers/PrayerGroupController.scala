package org.prayerping.controllers

import org.prayerping.forms.PrayerGroupForm
import org.prayerping.services._
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PrayerGroupController @Inject() (
  components: SilhouetteControllerComponents,
  prayerRequestService: PrayerRequestService,
  prayerFeedService: PrayerFeedService,
  prayerGroupService: PrayerGroupService,
  searchService: SearchService,
  cryptoService: CryptoService)(implicit ec: ExecutionContext) extends SilhouetteController(components) {

  def getPrayerGroups(query: Option[String], page: Int, pageSize: Int): Action[AnyContent] = Action.async { implicit request =>
    val prayerGroupsFuture = query match {
      case Some(q) => searchService.searchPrayerGroups(q, page, pageSize)
      case None => prayerGroupService.getPrayerGroups(page, pageSize)
    }
    prayerGroupsFuture map {
      prayerGroups => Ok(Json.toJson(prayerGroups))
    }
  }

  def createPrayerGroup: Action[AnyContent] = SecuredAction.async { implicit request =>
    PrayerGroupForm.form.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      data => {
        val salt = cryptoService.generateSalt
        val (publicKey, encryptedPrivateKey) = cryptoService.generateKeyPair(salt)
        prayerGroupService.createPrayerGroup(data.name, data.handle, None, data.description, request.identity.userID, publicKey, Some(encryptedPrivateKey), Some(salt)) map {
          prayerGroup => Ok(Json.toJson(prayerGroup))
        }
      }
    )
  }

  def updatePrayerGroup(groupId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    prayerGroupService.getPrayerGroup(groupId) flatMap {
      case None => Future.successful(NotFound)
      case Some(prayerGroup) => if (prayerGroup.whoCreated.userID == request.identity.userID) {
        PrayerGroupForm.form.bindFromRequest().fold(
          _ => Future.successful(BadRequest),
          data => prayerGroupService.updatePrayerGroup(groupId, data.handle, data.name, data.description) map {
            updatedPrayerGroup => Ok(Json.toJson(updatedPrayerGroup.getProfile))
          }
        )
      } else Future.successful(Forbidden)
    }
  }

  def getPrayerGroup(groupId: UUID): Action[AnyContent] = Action.async { _ =>
    prayerGroupService.getPrayerGroup(groupId) map {
      case None => NotFound
      case Some(prayerGroup) => Ok(Json.toJson(prayerGroup.getProfile))
    }
  }

  def joinPrayerGroup(groupId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    prayerGroupService.joinPrayerGroup(request.identity.userID, groupId) map {
      result => Ok(Json.toJson(result))
    }
  }

  def leavePrayerGroup(userId: UUID, groupId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    if (userId == request.identity.userID) {
      prayerGroupService.leavePrayerGroup(userId, groupId).map(_ => NoContent)
    } else Future.successful(Forbidden)
  }

  def getPrayerGroupMembers(groupId: UUID, page: Int, pageSize: Int): Action[AnyContent] = Action.async { _ =>
    prayerGroupService.getPrayerGroupMembers(groupId, page, pageSize) map {
      members => Ok(Json.toJson(members))
    }
  }

  def getGroupPrayerRequests(groupId: UUID, query: Option[String], page: Int,
    pageSize: Int): Action[AnyContent] = UserAwareAction.async { request =>
    val requesterId = request.identity.map(_.userID)
    val requestsPageFuture = query match {
      case Some(q) => searchService.searchGroupPrayerRequests(groupId, q, page, pageSize, requesterId)
      case None => prayerFeedService.getGroupPrayerRequests(groupId, page, pageSize, requesterId)
    }
    requestsPageFuture map {
      requestsPage => Ok(Json.toJson(requestsPage))
    }
  }

  def sharePrayerRequestWithGroup(requestId: UUID, groupId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    prayerRequestService.getPrayerRequest(requestId, Some(request.identity.userID)) flatMap {
      case None => Future.successful(NotFound)
      case Some(prayerRequest) => if (prayerRequest.canEdit) {
        prayerGroupService.sharePrayerRequestWithGroup(requestId, groupId) map {
          prayerRequestGroup => Ok(Json.toJson(prayerRequestGroup))
        }
      } else Future.successful(Forbidden)
    }
  }

  def removePrayerRequestFromGroup(requestId: UUID, groupId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    prayerRequestService.getPrayerRequest(requestId, Some(request.identity.userID)) flatMap {
      case None => Future.successful(NotFound)
      case Some(prayerRequest) => if (prayerRequest.canEdit) {
        prayerGroupService.removePrayerRequestFromGroup(requestId, groupId).map(_ => NoContent)
      } else Future.successful(Forbidden)
    }
  }

}
