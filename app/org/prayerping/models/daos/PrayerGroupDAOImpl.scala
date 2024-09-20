package org.prayerping.models.daos

import com.fasterxml.uuid.Generators
import org.prayerping.models.daos.PostgresProfile.api._
import org.prayerping.models._
import play.api.db.slick.DatabaseConfigProvider

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class PrayerGroupDAOImpl @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends PrayerGroupDAO with DAOSlick {

  private def getPrayerGroups(
    totalQuery: Query[PrayerGroupTable, PrayerGroupRow, Seq],
    page: Int, pageSize: Int): Future[Page[PrayerGroupProfile]] = {
    val offset = page * pageSize
    val paginatedGroups = totalQuery.drop(offset).take(pageSize)
    val withUser = paginatedGroups.join(userTableQuery).on(_.whoCreated === _.id)
    for {
      groups <- db.run(withUser.result) map {
        raw =>
          raw map {
            case (row, user) => PrayerGroupProfile(row.id, row.name, row.handle, row.domain, row.description,
              row.whenCreated, user.getPublicUserProfile, row.publicKey)
          }
      }
      totalCount <- db.run(totalQuery.length.result)
    } yield Page(groups, page, offset, totalCount.toLong)
  }

  /**
   * Gets a paginated list of prayer groups
   *
   * @param page     The page of the output
   * @param pageSize The page size in the output
   * @return A future of a page of prayer groups
   */
  def getPrayerGroups(page: Int, pageSize: Int): Future[Page[PrayerGroupProfile]] =
    getPrayerGroups(prayerGroupTableQuery.sortBy(_.name), page, pageSize)

  /**
   * Performs a full-text search of prayer groups
   *
   * @param query    The search string
   * @param page     The page of the search results
   * @param pageSize The page size in the search results
   * @return A future of a page of search results
   */
  def searchPrayerGroups(query: String, page: Int, pageSize: Int): Future[Page[PrayerGroupProfile]] =
    getPrayerGroups(prayerGroupTableQuery.filter(table => table.searchField @@ webSearchToTsQuery(query)), page,
      pageSize)

  private def getPrayerGroup(query: Query[PrayerGroupTable, PrayerGroupRow, Seq]): Future[Option[PrayerGroup]] = {
    val withUser = query.join(userTableQuery).on(_.whoCreated === _.id)
    db.run(withUser.result.headOption) map {
      raw =>
        raw map {
          case (row, user) => PrayerGroup(row.id, row.name, row.handle, row.domain, row.description, row.whenCreated,
            user.getPublicUserProfile, row.publicKey, row.privateKey, row.salt)
        }
    }
  }
  /**
   * Gets a specific prayer group
   *
   * @param groupId The ID of the group
   * @return A future of an option of a matching prayer group
   */
  def getPrayerGroup(groupId: UUID): Future[Option[PrayerGroup]] =
    getPrayerGroup(prayerGroupTableQuery.filter(_.id === groupId))

  /**
   * Gets a prayer group by its handle
   *
   * @param handle  The handle of the group to find
   * @return  The found prayer group or none if a group cannot be found
   */
  def getPrayerGroup(handle: String): Future[Option[PrayerGroup]] =
    getPrayerGroup(prayerGroupTableQuery.filter(_.handle === handle))

  /**
   * Creates a prayer group
   *
   * @param name        The group's name
   * @param handle        The group's handle
   * @param description The group's description
   * @param whoCreated  The ID of the group's creator
   * @return A future of the created prayer group
   */
  def createPrayerGroup(name: String, handle: String, domain: Option[String], description: String, whoCreated: UUID,
    publicKey: String, privateKey: Option[String], salt: Option[Array[Byte]]): Future[PrayerGroupProfile] = {
    val prayerGroupRow = PrayerGroupRow(Generators.timeBasedReorderedGenerator().generate(), name, handle, domain,
      description, Instant.now(), whoCreated, publicKey, privateKey, salt)
    val handleRow = (handle, domain, EntityType.Group, prayerGroupRow.id, Instant.now())
    db.run(DBIO.seq(
      handleTableQuery += handleRow,
      prayerGroupTableQuery += prayerGroupRow).transactionally)
      .flatMap(_ => getPrayerGroup(prayerGroupRow.id).map(_.get.getProfile))
  }

  /**
   * Updates a prayer group's name and description
   *
   * @param groupId     The ID of the group
   * @param handle      The group's updated handle
   * @param name        The group's updated name
   * @param description The group's updated description
   * @return A future of the updated prayer group
   */
  def updatePrayerGroup(groupId: UUID, handle: String, name: String, description: String): Future[PrayerGroup] = {
    val prayerGroupQuery = for { prayerGroup <- prayerGroupTableQuery if prayerGroup.id === groupId } yield {
      (prayerGroup.handle, prayerGroup.name, prayerGroup.description)
    }
    val handleQuery = for {
      handleRecord <- handleTableQuery if handleRecord.entityId === groupId &&
        handleRecord.entityType === EntityType.Group
    } yield handleRecord.handle
    db.run(DBIO.seq(
      handleQuery.update(handle),
      prayerGroupQuery.update((handle, name, description))
    ).transactionally).flatMap(_ => getPrayerGroup(groupId).map(_.get))
  }

  /**
   * Joins a user to a prayer group
   *
   * @param userId  The ID of the user
   * @param groupId The ID of the group
   * @return A future of an object representing the membership
   */
  def joinPrayerGroup(userId: UUID, groupId: UUID): Future[PrayerGroupMembership] = {
    val newRow = PrayerGroupMembershipRow(groupId, userId, Instant.now())
    db.run(prayerGroupMembershipTableQuery += newRow) flatMap {
      _ =>
        db.run(prayerGroupMembershipTableQuery.filter(row => row.userId === userId && row.groupId === groupId)
          .join(userTableQuery).on(_.userId === _.id).result.head)
          .map {
            case (row, user) => PrayerGroupMembership(row.groupId, user.getPublicUserProfile, row.whenCreated)
          }
    }
  }

  /**
   * Removes a user from a prayer group
   * @param userId    The ID of the user
   * @param groupId   The ID of the group
   * @return  A future that completes once the user has been removed from the group
   */
  def leavePrayerGroup(userId: UUID, groupId: UUID): Future[Unit] = {
    db.run(prayerGroupMembershipTableQuery.filter(row => row.userId === userId && row.groupId === groupId).delete)
      .map(_ => ())
  }

  /**
   * Gets the members of a prayer group
   *
   * @param groupId  The ID of the prayer group
   * @param page     The page of the results
   * @param pageSize The page size of the results
   * @return A future of a paginated list of member users
   */
  def getPrayerGroupMembers(groupId: UUID, page: Int, pageSize: Int): Future[Page[PrayerGroupMembership]] = {
    val offset = page * pageSize
    val totalQuery = prayerGroupMembershipTableQuery.filter(_.groupId === groupId).join(userTableQuery)
      .on(_.userId === _.id)
    val pageOfResults = totalQuery.drop(offset).take(pageSize)
    for {
      users <- db.run(pageOfResults.result) map {
        raw =>
          raw map {
            case (row, user) => PrayerGroupMembership(row.groupId, user.getPublicUserProfile, row.whenCreated)
          }
      }
      totalCount <- db.run(totalQuery.length.result)
    } yield Page(users, page, offset, totalCount.toLong)
  }

  /**
   * Gets the members of a prayer group
   *
   * @param groupId The Id of the prayer group
   * @return A future of a sequence of member users
   */
  def getPrayerGroupMembers(groupId: UUID): Future[Seq[PrayerGroupMembership]] = {
    val query = prayerGroupMembershipTableQuery.filter(_.groupId === groupId)
      .join(userTableQuery)
      .on(_.userId === _.id)

    db.run(query.result) map {
      raw =>
        raw map {
          case (row, user) => PrayerGroupMembership(row.groupId, user.getPublicUserProfile, row.whenCreated)
        }
    }

  }
}
