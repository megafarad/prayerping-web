package org.prayerping.models.daos

import com.github.tminglei.slickpg.TsVector
import org.prayerping.models._
import org.prayerping.models.daos.PostgresProfile.api._
import play.api.libs.json.JsValue
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

trait AppTableDefinitions { self: AuthTableDefinitions =>

  implicit val visibilityColumType: BaseColumnType[Visibility.Value] = MappedColumnType.base[Visibility.Value, String](
    e => e.toString,
    s => Visibility.withName(s)
  )

  case class PrayerRequestRow(id: UUID, userId: UUID, request: String, isAnonymous: Boolean, whenCreated: Instant,
    visibility: Visibility.Value)

  class PrayerRequestTable(tag: Tag) extends Table[PrayerRequestRow](tag, Some("app"), "prayer_request") {
    def id = column[UUID]("id", O.PrimaryKey)
    def userId = column[UUID]("user_id")
    def request = column[String]("request", O.SqlType("text"))
    def isAnonymous = column[Boolean]("is_anonymous")
    def whenCreated = column[Instant]("when_created")
    def visibility = column[Visibility.Value]("visibility")
    def searchField = column[TsVector]("search_field", O.SqlType("tsvector"))
    def user = foreignKey("app_prayer_request_user_id_fk", userId, userTableQuery)(_.id)
    def * : ProvenShape[PrayerRequestRow] = (id, userId, request, isAnonymous, whenCreated,
      visibility).mapTo[PrayerRequestRow]
  }

  val prayerRequestTableQuery = TableQuery[PrayerRequestTable]

  implicit val reactionTypeColumnType: BaseColumnType[ReactionType.Value] =
    MappedColumnType.base[ReactionType.Value, String](
      e => e.toString,
      s => ReactionType.withName(s)
    )

  case class PrayerRequestReactionRow(id: UUID, requestId: UUID, userId: UUID, reactionType: ReactionType.Value)

  class PrayerRequestReactionTable(tag: Tag) extends Table[PrayerRequestReactionRow](tag, Some("app"), "prayer_request_reaction") {
    def id = column[UUID]("id", O.PrimaryKey)
    def requestId = column[UUID]("request_id")
    def userId = column[UUID]("user_id")
    def reactionType = column[ReactionType.Value]("reaction_type")
    def request = foreignKey("app_prayer_request_reaction_request_id_fk", requestId, prayerRequestTableQuery)(_.id)
    def user = foreignKey("app_prayer_request_reaction_user_id_fk", userId, userTableQuery)(_.id)
    def * : ProvenShape[PrayerRequestReactionRow] = (id, requestId, userId, reactionType).mapTo[PrayerRequestReactionRow]
  }

  val prayerRequestReactionTableQuery = TableQuery[PrayerRequestReactionTable]

  case class PrayerResponseRow(id: UUID, requestId: UUID, userId: UUID, response: String, whenCreated: Instant)

  class PrayerResponseTable(tag: Tag) extends Table[PrayerResponseRow](tag, Some("app"), "prayer_response") {
    def id = column[UUID]("id", O.PrimaryKey)
    def requestId = column[UUID]("request_id")
    def userId = column[UUID]("user_id")
    def response = column[String]("response", O.SqlType("text"))
    def searchField = column[TsVector]("search_field", O.SqlType("tsvector"))
    def whenCreated = column[Instant]("when_created")
    def request = foreignKey("app_prayer_response_request_id_fk", requestId, prayerRequestTableQuery)(_.id)
    def user = foreignKey("app_prayer_response_user_id_fk", userId, userTableQuery)(_.id)
    def * : ProvenShape[PrayerResponseRow] = (id, requestId, userId, response, whenCreated).mapTo[PrayerResponseRow]
  }

  val prayerResponseTableQuery = TableQuery[PrayerResponseTable]

  case class PrayerResponseReactionRow(id: UUID, responseId: UUID, userId: UUID, reactionType: ReactionType.Value)

  class PrayerResponseReactionTable(tag: Tag) extends Table[PrayerResponseReactionRow](tag, Some("app"), "prayer_response_reaction") {
    def id = column[UUID]("id", O.PrimaryKey)
    def responseId = column[UUID]("response_id")
    def userId = column[UUID]("user_id")
    def reactionType = column[ReactionType.Value]("reaction_type")
    def response = foreignKey("app_prayer_response_reaction_response_id_fk", responseId, prayerResponseTableQuery)(_.id)
    def user = foreignKey("app_prayer_response_reaction_user_id_fk", userId, userTableQuery)(_.id)
    def * : ProvenShape[PrayerResponseReactionRow] = (id, responseId, userId, reactionType).mapTo[PrayerResponseReactionRow]
  }

  val prayerResponseReactionTableQuery = TableQuery[PrayerResponseReactionTable]

  case class PrayerGroupRow(id: UUID, name: String, handle: String, domain: Option[String], description: String,
    whenCreated: Instant, whoCreated: UUID, publicKey: String, privateKey: Option[String],
    salt: Option[Array[Byte]])

  class PrayerGroupTable(tag: Tag) extends Table[PrayerGroupRow](tag, Some("app"), "prayer_group") {
    def id = column[UUID]("id", O.PrimaryKey)
    def name = column[String]("name")
    def handle = column[String]("handle", O.SqlType("CITEXT"))
    def domain = column[Option[String]]("domain")
    def description = column[String]("description", O.SqlType("text"))
    def searchField = column[TsVector]("search_field", O.SqlType("tsvector"))
    def whenCreated = column[Instant]("when_created")
    def whoCreated = column[UUID]("who_created")
    def publicKey = column[String]("public_key")
    def privateKey = column[Option[String]]("private_key")
    def salt = column[Option[Array[Byte]]]("salt")
    def whoCreatedUser = foreignKey("app_prayer_group_who_created", whoCreated, userTableQuery)(_.id)
    def idx = index("app_prayer_group_unique", (handle, domain), unique = true)
    def * : ProvenShape[PrayerGroupRow] = (id, name, handle, domain, description, whenCreated, whoCreated, publicKey,
      privateKey, salt).mapTo[PrayerGroupRow]
  }

  val prayerGroupTableQuery = TableQuery[PrayerGroupTable]

  case class PrayerGroupMembershipRow(groupId: UUID, userId: UUID, whenCreated: Instant)

  class PrayerGroupMembershipTable(tag: Tag) extends Table[PrayerGroupMembershipRow](tag, Some("app"), "prayer_group_membership") {
    def groupId = column[UUID]("group_id")
    def userId = column[UUID]("user_id")
    def whenCreated = column[Instant]("when_created")
    def pk = primaryKey("prayer_group_membership_pk", (groupId, userId))
    def group = foreignKey("app_prayer_group_membership_group_id", groupId, prayerGroupTableQuery)(_.id)
    def user = foreignKey("app_prayer_group_membership_user_id", userId, userTableQuery)(_.id)
    def * : ProvenShape[PrayerGroupMembershipRow] = (groupId, userId, whenCreated).mapTo[PrayerGroupMembershipRow]
  }

  val prayerGroupMembershipTableQuery = TableQuery[PrayerGroupMembershipTable]

  class PrayerRequestGroupTable(tag: Tag) extends Table[PrayerRequestGroup](tag, Some("app"), "prayer_request_group") {
    def requestId = column[UUID]("request_id")
    def groupId = column[UUID]("group_id")
    def pk = primaryKey("prayer_request_group_pk", (requestId, groupId))
    def request = foreignKey("app_prayer_request_group_request_id", requestId, prayerRequestTableQuery)(_.id)
    def group = foreignKey("app_prayer_request_group_group_id", groupId, prayerGroupTableQuery)(_.id)
    def * : ProvenShape[PrayerRequestGroup] = (requestId, groupId).mapTo[PrayerRequestGroup]
  }

  val prayerRequestGroupTableQuery = TableQuery[PrayerRequestGroupTable]

  implicit val entityTypeColumnType: BaseColumnType[EntityType.Value] = MappedColumnType.base[EntityType.Value, String](
    e => e.toString,
    s => EntityType.withName(s)
  )

  class HandleTable(tag: Tag) extends Table[(String, Option[String], EntityType.Value, UUID, Instant)](tag, Some("app"), "handle") {
    def handle = column[String]("handle", O.SqlType("CITEXT"))
    def domain = column[Option[String]]("domain")
    def entityType = column[EntityType.Value]("entity_type")
    def entityId = column[UUID]("entity_id")
    def whenCreated = column[Instant]("when_created")
    def pk = primaryKey("handle_pk", (entityType, entityId))
    def * : ProvenShape[(String, Option[String], EntityType.Value, UUID, Instant)] = (handle, domain, entityType, entityId, whenCreated)
  }

  val handleTableQuery = TableQuery[HandleTable]

  case class PrayerRequestMentionRow(id: UUID, requestId: UUID, handle: String, domain: Option[String],
    whenCreated: Instant)

  class PrayerRequestMentionTable(tag: Tag) extends Table[PrayerRequestMentionRow](tag, Some("app"),
    "prayer_request_mention") {
    def id = column[UUID]("id", O.PrimaryKey)
    def requestId = column[UUID]("request_id")
    def handle = column[String]("handle", O.SqlType("CITEXT"))
    def domain = column[Option[String]]("domain")
    def whenCreated = column[Instant]("when_created")
    def request = foreignKey(
      "app_prayer_request_mention_request_id_fk",
      requestId, prayerRequestTableQuery)(_.id)
    def handleFK = foreignKey(
      "app_prayer_request_mention_handle_domain_fk",
      (handle, domain), handleTableQuery)(handleTable => (handleTable.handle, handleTable.domain))
    def * : ProvenShape[PrayerRequestMentionRow] = (id, requestId, handle, domain,
      whenCreated).mapTo[PrayerRequestMentionRow]
  }

  val prayerRequestMentionTableQuery = TableQuery[PrayerRequestMentionTable]

  case class PrayerResponseMentionRow(id: UUID, responseId: UUID, handle: String, domain: Option[String],
    whenCreated: Instant)

  class PrayerResponseMentionTable(tag: Tag) extends Table[PrayerResponseMentionRow](tag, Some("app"),
    "prayer_response_mention") {
    def id = column[UUID]("id", O.PrimaryKey)
    def responseId = column[UUID]("response_id")
    def handle = column[String]("handle", O.SqlType("CITEXT"))
    def domain = column[Option[String]]("domain")
    def whenCreated = column[Instant]("when_created")
    def request = foreignKey(
      "app_prayer_response_mention_response_id_fk",
      responseId, prayerResponseTableQuery)(_.id)
    def handleFK = foreignKey(
      "app_prayer_response_mention_handle_domain_fk",
      (handle, domain), handleTableQuery)(handleTable => (handleTable.handle, handleTable.domain))
    def * : ProvenShape[PrayerResponseMentionRow] = (id, responseId, handle, domain,
      whenCreated).mapTo[PrayerResponseMentionRow]
  }

  val prayerResponseMentionTableQuery = TableQuery[PrayerResponseMentionTable]

  class FollowTable(tag: Tag) extends Table[(UUID, UUID, Instant)](tag, Some("app"), "follow") {
    def userId = column[UUID]("user_id")
    def targetUserId = column[UUID]("target_user_id")
    def whenCreated = column[Instant]("when_created")
    def pk = primaryKey("follow_pk", (userId, targetUserId))
    def * : ProvenShape[(UUID, UUID, Instant)] = (userId, targetUserId, whenCreated)
  }

  val followTableQuery = TableQuery[FollowTable]

}