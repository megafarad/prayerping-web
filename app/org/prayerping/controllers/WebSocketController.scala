package org.prayerping.controllers

import org.apache.pekko.actor.{ ActorRef, ActorSystem }
import org.apache.pekko.stream.Materializer
import org.prayerping.actor.WebSocketActor
import play.api.Configuration
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.silhouette.api.HandlerResult

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

class WebSocketController @Inject() (
  scc: SilhouetteControllerComponents,
  @Named("messageDispatcherActor") dispatcher: ActorRef)(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext)
  extends AbstractAuthController(scc) {

  def socket: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    implicit val req: Request[AnyContentAsEmpty.type] = Request(request, AnyContentAsEmpty)
    silhouette.SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.map {
      case HandlerResult(_, Some(user)) => Right(ActorFlow.actorRef(out => WebSocketActor.props(out, dispatcher, user.userID)))
      case HandlerResult(r, None) => Left(r)
    }

  }

}
