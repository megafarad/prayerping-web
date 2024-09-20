package org.prayerping.services

import org.prayerping.models._

import java.util.UUID
import scala.concurrent.Future

trait PrayerGroupService {

  /**
   * Gets a paginated list of prayer groups
   *
   * @param page The page of the output
   * @param pageSize The page size in the output
   * @return A future of a page of prayer groups
   */
  def getPrayerGroups(page: Int, pageSize: Int): Future[Page[PrayerGroupProfile]]

  /**
   * Gets a specific prayer group
   * @param groupId The ID of the group
   * @return A future of an option of a matching prayer group
   */
  def getPrayerGroup(groupId: UUID): Future[Option[PrayerGroup]]

  /**
   * Creates a prayer group
   *
   * @param name          The group's name
   * @param handle        The group's handle
   * @param description   The group's description
   * @param whoCreated    The ID of the group's creator
   * @return  A future of the created prayer group
   */
  def createPrayerGroup(name: String, handle: String, domain: Option[String], description: String, whoCreated: UUID, publicKey: String, privateKey: Option[String], salt: Option[Array[Byte]]): Future[PrayerGroupProfile]

  /**
   * Updates a prayer group's name and description
   *
   * @param groupId       The ID of the group
   * @param name          The group's updated name
   * @param description   The group's updated description
   * @return  A future of the updated prayer group
   */
  def updatePrayerGroup(groupId: UUID, handle: String, name: String, description: String): Future[PrayerGroup]

  /**
   * Joins a user to a prayer group
   *
   * @param userId    The ID of the user
   * @param groupId   The ID of the group
   * @return  A future of an object representing the membership
   */
  def joinPrayerGroup(userId: UUID, groupId: UUID): Future[PrayerGroupMembership]

  /**
   * Removes a user from a prayer group
   * @param userId    The ID of the user
   * @param groupId   The ID of the group
   * @return  A future that completes once the user has been removed from the group
   */
  def leavePrayerGroup(userId: UUID, groupId: UUID): Future[Unit]

  /**
   * Gets the members of a prayer group
   *
   * @param groupId   The ID of the prayer group
   * @param page      The page of the results
   * @param pageSize  The page size of the results
   * @return  A future of a paginated list of member users
   */
  def getPrayerGroupMembers(groupId: UUID, page: Int, pageSize: Int): Future[Page[PrayerGroupMembership]]

  /**
   * Shares a prayer request with a group
   *
   * @param requestId   The ID of the prayer request
   * @param groupId     The ID of the prayer group
   * @return A future of an object representing the share
   */
  def sharePrayerRequestWithGroup(requestId: UUID, groupId: UUID): Future[PrayerRequestGroup]

  /**
   * Removes a prayer request from a group
   *
   * @param requestId   The ID of the prayer request
   * @param groupId     The ID of the prayer group
   * @return
   */
  def removePrayerRequestFromGroup(requestId: UUID, groupId: UUID): Future[Unit]

}
