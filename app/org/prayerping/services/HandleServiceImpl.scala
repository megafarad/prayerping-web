package org.prayerping.services

import org.prayerping.models.daos.{ HandleDAO, PrayerGroupDAO, UserDAO }
import org.prayerping.models.{ EntityType, GroupHandle, Handle, UserHandle }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class HandleServiceImpl @Inject() (handleDAO: HandleDAO, userDAO: UserDAO,
  prayerGroupDAO: PrayerGroupDAO)(implicit ec: ExecutionContext) extends HandleService {

  /**
   * Gets a handle if it exists
   *
   * @param handle The handle to get
   * @return A handle with the object it points to
   */
  def getHandle(handle: String, domain: Option[String]): Future[Option[Handle]] =
    handleDAO.getRawHandle(handle, domain) flatMap {
      case Some(rawHandle) => rawHandle._3 match {
        case EntityType.User => userDAO.find(rawHandle._4) map {
          matchingUser =>
            matchingUser map {
              user => UserHandle(user.getPublicUserProfile)
            }
        }
        case EntityType.Group => prayerGroupDAO.getPrayerGroup(rawHandle._4) map {
          matchingGroup =>
            matchingGroup map {
              group => GroupHandle(group.getProfile)
            }
        }
        case _ => Future.successful(None)
      }
      case None => Future.successful(None)
    }

  /**
   * Gets suggestions for matching handles
   *
   * @param queryString The string to search handles for
   * @return A seq of strings that match the search string
   */
  def getHandleSuggestions(queryString: String): Future[Seq[String]] =
    handleDAO.getHandleSuggestions(queryString)
}
