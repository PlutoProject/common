package ink.pmc.common.member.impl

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.benmanes.caffeine.cache.Caffeine
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import ink.pmc.common.member.api.Comment
import ink.pmc.common.member.api.Member
import ink.pmc.common.member.api.MemberAPI
import ink.pmc.common.member.api.MemberManager
import ink.pmc.common.member.api.dsl.MemberDSL
import ink.pmc.common.member.api.punishment.Punishment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.bson.Document
import org.mongojack.JacksonMongoCollection
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture

class MemberManagerImpl(
    override val collection: JacksonMongoCollection<Member>,
    override val punishmentIndexCollection: MongoCollection<Document>,
    override val commentIndexCollection: MongoCollection<Document>
) : MemberManager {

    private val cacheLoader: CacheLoader<UUID, Member> = CacheLoader<UUID, Member> {
        collection.find(Filters.eq("uuid", it)).first()
    }

    override val cachedMember: AsyncLoadingCache<UUID, Member> = Caffeine.newBuilder()
        .maximumSize(10000)
        .refreshAfterWrite(Duration.ofMinutes(5))
        .expireAfterWrite(Duration.ofMinutes(10))
        .buildAsync(cacheLoader)

    init {
        if (punishmentIndexCollection.find(Filters.exists("lastId")).first() == null) {
            val document = Document(mapOf("lastId" to -1L))
            punishmentIndexCollection.insertOne(document)
        }

        if (commentIndexCollection.find(Filters.exists("lastId")).first() == null) {
            val document = Document(mapOf("lastId" to -1L, "emptyIds" to listOf<Long>()))
            commentIndexCollection.insertOne(document)
        }
    }

    override suspend fun register(member: Member) {
        if (exist(member.uuid)) {
            return
        }

        insertAndUpdateCache(member)
    }

    override suspend fun createAndRegister(block: MemberDSL.() -> Unit): Member {
        val member = MemberAPI.instance.createMember(block)
        register(member)

        return member
    }

    override suspend fun update(member: Member): Boolean {
        return withContext(Dispatchers.IO) {
            if (nonExist(member.uuid)) {
                return@withContext false
            }

            collection.deleteOne(Filters.eq("uuid", member.uuid))
            insertAndUpdateCache(member)

            true
        }
    }

    override suspend fun get(uuid: UUID): Member? {
        return withContext(Dispatchers.IO) {
            cachedMember.get(uuid).await()
        }
    }

    override suspend fun remove(uuid: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            if (nonExist(uuid)) {
                return@withContext false
            }

            val member = get(uuid)!!

            removeRelatedComments(member)
            collection.deleteOne(Filters.eq("uuid", uuid))
            true
        }
    }

    override suspend fun exist(uuid: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            cachedMember.getIfPresent(uuid) != null || collection.find(Filters.eq("uuid", uuid)).first() != null
        }
    }

    override suspend fun nonExist(uuid: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            collection.find(Filters.eq("uuid", uuid)).first() == null
        }
    }

    override suspend fun lookupPunishment(id: Long): Punishment? {
        return withContext(Dispatchers.IO) {
            val document = punishmentIndexCollection.find(Filters.eq("id", id)).first() ?: return@withContext null
            val ownerUUID = document["owner"] as UUID
            get(ownerUUID)!!.getPunishment(id)
        }
    }

    override suspend fun lookupComment(id: Long): Comment? {
        return withContext(Dispatchers.IO) {
            val document = commentIndexCollection.find(Filters.eq("id", id)).first() ?: return@withContext null
            val ownerUUID = document["owner"] as UUID
            get(ownerUUID)!!.getComment(id)
        }
    }

    override suspend fun sync(member: Member): Boolean {
        return withContext(Dispatchers.IO) {
            if (nonExist(member.uuid)) {
                return@withContext false
            }

            try {
                cachedMember.synchronous().refresh(member.uuid).await()
            } catch (e: Exception) {
                return@withContext false
            }

            true
        }
    }

    override suspend fun syncAll(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                cachedMember.synchronous().asMap().keys.forEach {
                    if (nonExist(it)) {
                        return@forEach
                    }

                    cachedMember.synchronous().refresh(it).await()
                }
            } catch (e: Exception) {
                return@withContext false
            }

            true
        }
    }

    private fun insertAndUpdateCache(member: Member) {
        collection.insertOne(member)
        cachedMember.put(member.uuid, CompletableFuture.completedFuture(member))
    }

    private fun removeRelatedComments(member: Member) {
        val uuid = member.uuid
        commentIndexCollection.deleteMany(Filters.eq("owner", uuid))
    }

}