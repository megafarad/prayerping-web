package org.prayerping.controllers

import org.prayerping.forms.ReactionForm
import org.prayerping.services.ReactionService
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent }

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PrayerReactionController @Inject() (
  components: SilhouetteControllerComponents,
  reactionService: ReactionService)(implicit ec: ExecutionContext) extends SilhouetteController(components) {
  def getPrayerRequestReactions(requestId: UUID): Action[AnyContent] = Action.async { _ =>
    reactionService.getPrayerRequestReactions(requestId) map {
      reactions => Ok(Json.toJson(reactions))
    }
  }

  def createPrayerRequestReaction(requestId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    ReactionForm.form.bindFromRequest().fold(
      response => {
        logger.info(response.errors.mkString)
        Future.successful(BadRequest)
      },
      data => reactionService.createPrayerRequestReaction(requestId, request.identity.userID, data.reactionType) map {
        prayerRequestReaction => Ok(Json.toJson(prayerRequestReaction))
      }
    )
  }

  def deletePrayerRequestReaction(
    requestId: UUID,
    reactionId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    reactionService.getPrayerRequestReaction(reactionId) flatMap {
      case None => Future.successful(NotFound)
      case Some(reaction) => if (request.identity.userID == reaction.user.userID) {
        reactionService.deletePrayerRequestReaction(reaction) map {
          _ => NoContent
        }
      } else Future.successful(Forbidden)
    }
  }

  def getPrayerResponseReactions(responseId: UUID): Action[AnyContent] = Action.async { _ =>
    reactionService.getPrayerResponseReactions(responseId) map {
      reactions => Ok(Json.toJson(reactions))
    }
  }

  def createPrayerResponseReaction(responseId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    ReactionForm.form.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      data => reactionService.createPrayerResponseReaction(responseId, request.identity.userID, data.reactionType) map {
        reaction => Ok(Json.toJson(reaction))
      }
    )
  }

  def deletePrayerResponseReaction(responseId: UUID, reactionId: UUID): Action[AnyContent] = SecuredAction.async { implicit request =>
    reactionService.getPrayerResponseReaction(reactionId) flatMap {
      case None => Future.successful(NotFound)
      case Some(reaction) => if (reaction.user.userID == request.identity.userID) {
        reactionService.deletePrayerResponseReaction(reaction) map {
          _ => NoContent
        }
      } else Future.successful(Forbidden)
    }
  }

}
