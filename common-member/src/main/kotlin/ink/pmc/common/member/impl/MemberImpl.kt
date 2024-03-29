package ink.pmc.common.member.impl

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import ink.pmc.common.member.api.Comment
import ink.pmc.common.member.api.Member
import ink.pmc.common.member.api.MemberAPI
import ink.pmc.common.member.api.MemberData
import ink.pmc.common.member.api.dsl.PunishmentOptionsDSL
import ink.pmc.common.member.api.punishment.PardonReason
import ink.pmc.common.member.api.punishment.Punishment
import ink.pmc.common.member.api.punishment.PunishmentOptions
import ink.pmc.common.member.api.punishment.PunishmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.mongojack.ObjectId
import java.util.*

@Suppress("UNUSED", "UNCHECKED_CAST")
data class MemberImpl @JsonCreator constructor(
    @JsonProperty("uuid") override val uuid: UUID,
    @JsonProperty("name") override var name: String
) : Member {

    @ObjectId
    @JsonProperty("_id")
    var id: String? = null

    override var joinTime: Date = Date()
    override var lastJoinTime: Date? = null
    override var lastQuitTime: Date? = null
    override val punishments: MutableCollection<Punishment> = mutableSetOf()
    override var currentPunishmentId: Long? = null
    override var lastPunishmentId: Long? = null
    override val comments: MutableCollection<Comment> = mutableSetOf()
    override val data: MemberData = MemberDataImpl()
    override var bio: String? = null
    override var totalPlayTime: Long = 0L

    override suspend fun punish(options: PunishmentOptions): Punishment {
        return withContext(Dispatchers.IO) {
            val collection = MemberAPI.instance.memberManager.punishmentIndexCollection
            val document = collection.find(Filters.exists("lastId")).first()
            val newId = document!!.getLong("lastId") + 1

            val punishment = PunishmentImpl(newId, uuid, options.type)
            punishment.reason = options.reason

            punishments.add(punishment)

            if (options.type != PunishmentType.WARN_A && options.type != PunishmentType.WARN_B) {
                currentPunishmentId = newId
            }

            lastPunishmentId = newId

            collection.insertOne(Document(mapOf<String, Any>("id" to newId, "owner" to uuid)))
            collection.updateOne(Filters.exists("lastId"), Updates.inc("lastId", 1))

            punishment
        }
    }

    override suspend fun punish(block: PunishmentOptionsDSL.() -> Unit): Punishment {
        val dsl = PunishmentOptionsDSL()
        dsl.block()

        if (dsl.type == null) {
            throw RuntimeException("Required information missed")
        }

        val options = PunishmentOptions(dsl.type!!)
        options.reason = dsl.reason

        return punish(options)
    }

    override fun pardon(reason: PardonReason): Boolean {
        if (currentPunishment == null) {
            return false
        }

        currentPunishment!!.isPardoned = true
        currentPunishment!!.pardonReason = reason

        return true
    }

    override fun getPunishment(id: Long): Punishment? {
        if (!punishments.any { it.id == id }) {
            return null
        }

        return punishments.find { it.id == id }
    }


    override suspend fun createComment(creator: UUID, content: String): Comment {
        return withContext(Dispatchers.IO) {
            val collection = MemberAPI.instance.memberManager.commentIndexCollection

            val emptyIds = collection.find(Filters.exists("emptyIds")).first()?.get(
                "emptyIds"
            ) as? MutableList<Long>

            val newId = if (emptyIds!!.isEmpty()) {
                collection.find(Filters.exists("lastId")).first()?.getLong("lastId")?.plus(1)
            } else {
                emptyIds.first()
            }

            if (emptyIds.isNotEmpty()) {
                emptyIds.removeFirst()
                collection.updateOne(Filters.exists("emptyIds"), Updates.set("emptyIds", emptyIds))
            }

            val comment = CommentImpl(newId!!, uuid, creator, content)
            comments.add(comment)

            collection.insertOne(Document(mapOf<String, Any>("id" to newId, "owner" to uuid)))

            if (emptyIds.isEmpty()) {
                collection.updateOne(Filters.eq("lastId", newId - 1), Updates.inc("lastId", 1))
            }

            comment
        }
    }

    override suspend fun removeComment(id: Long): Boolean {
        return withContext(Dispatchers.IO) {
            comments.removeIf {
                it.id == id
            }

            val collection = MemberAPI.instance.memberManager.commentIndexCollection
            val emptyIds = collection.find(Filters.exists("emptyIds")).first()?.get(
                "emptyIds"
            ) as? MutableList<Long>

            emptyIds?.add(id)

            collection.updateOne(Filters.exists("emptyIds"), Updates.set("emptyIds", emptyIds))
            collection.deleteOne(Filters.eq("id", id))

            true
        }
    }

    override fun updateComment(id: Long, content: String): Boolean {
        val comment = comments.first {
            it.id == id
        }

        comment.content = content
        comment.isModified = true

        return true
    }

    override fun getComment(id: Long): Comment? {
        if (!comments.any { it.id == id }) {
            return null
        }

        return comments.first { it.id == id }
    }

    override fun increasePlayTime(ms: Long) {
        totalPlayTime += ms
    }

    override suspend fun update() {
        MemberAPI.instance.memberManager.update(this)
    }

    override suspend fun sync() {
        MemberAPI.instance.memberManager.sync(this)
    }

}