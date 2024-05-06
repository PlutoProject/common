package ink.pmc.common.member

import com.github.benmanes.caffeine.cache.AsyncCacheLoader
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import ink.pmc.common.member.api.AuthType
import ink.pmc.common.member.api.Member
import ink.pmc.common.member.api.WhitelistStatus
import ink.pmc.common.member.api.data.MemberModifier
import ink.pmc.common.member.comment.AbstractCommentRepository
import ink.pmc.common.member.data.AbstractBedrockAccount
import ink.pmc.common.member.data.AbstractDataContainer
import ink.pmc.common.member.data.BedrockAccountImpl
import ink.pmc.common.member.data.DataContainerImpl
import ink.pmc.common.member.storage.*
import ink.pmc.common.utils.bedrock.xuid
import ink.pmc.common.utils.concurrent.submitAsyncIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

@Suppress("UNUSED")
const val UID_START = 10000L

@Suppress("UNUSED")
class MemberServiceImpl(
    database: MongoDatabase
) : AbstractMemberService() {

    override val status: MongoCollection<StatusStorage> = database.getCollection("member_status")
    override val members: MongoCollection<MemberStorage> = database.getCollection("member_members")
    override val punishments: MongoCollection<PunishmentStorage> = database.getCollection("member_punishments")
    override val comments: MongoCollection<CommentStorage> = database.getCollection("member_comments")
    override val dataContainers: MongoCollection<DataContainerStorage> =
        database.getCollection("member_data_containers")
    override val bedrockAccounts: MongoCollection<BedrockAccountStorage> =
        database.getCollection("member_bedrock_accounts")
    private val cacheLoader =
        AsyncCacheLoader<Long, Member?> { key, _ -> submitAsyncIO<Member?> { loadMember(key) }.asCompletableFuture() }
    override val loadedMembers: AsyncLoadingCache<Long, Member?> = Caffeine.newBuilder()
        .refreshAfterWrite(Duration.ofMinutes(10))
        .expireAfterWrite(Duration.ofMinutes(10))
        .buildAsync(cacheLoader)
    override lateinit var currentStatus: StatusStorage

    private val updateOptions = ReplaceOptions().upsert(true)

    override suspend fun lookupMemberStorage(uid: Long): MemberStorage? {
        return members.find(eq("uid", uid)).firstOrNull()
    }

    override suspend fun lookupPunishmentStorage(id: Long): PunishmentStorage? {
        return punishments.find(eq("id", id)).firstOrNull()
    }

    override suspend fun lookupCommentStorage(id: Long): CommentStorage? {
        return comments.find(eq("id", id)).firstOrNull()
    }

    override suspend fun lookupDataContainerStorage(id: Long): DataContainerStorage? {
        return dataContainers.find(eq("id", id)).firstOrNull()
    }

    override suspend fun lookupBedrockAccount(id: Long): BedrockAccountStorage? {
        return bedrockAccounts.find(eq("id", id)).firstOrNull()
    }

    private suspend fun loadMember(uid: Long): Member? {
        val memberStorage = members.find(eq("uid", uid)).firstOrNull() ?: return null
        return createMemberInstance(memberStorage)
    }

    private val service = this

    private suspend fun createMemberInstance(storage: MemberStorage): Member = withContext(Dispatchers.IO) {
        MemberImpl(service, storage).apply {
            dataContainer = DataContainerImpl(this, lookupDataContainerStorage(storage.dataContainer)!!)
            if (storage.bedrockAccount != null) {
                bedrockAccount = BedrockAccountImpl(this, lookupBedrockAccount(storage.bedrockAccount!!)!!)
            }
        }
    }

    override suspend fun lastUid(): Long {
        return currentStatus.lastMember
    }

    override suspend fun lastMember(): Member? {
        return lookup(currentStatus.lastMember)
    }

    override suspend fun lastMemberCreatedAt(): Instant? {
        return lastMember()?.createdAt
    }

    init {
        var lookupStatus: StatusStorage?
        submitAsyncIO {
            lookupStatus = status.find(exists("lastMember")).firstOrNull()
            if (lookupStatus == null) {
                lookupStatus = StatusStorage(ObjectId(), -1, -1, -1, -1, -1)
                status.insertOne(lookupStatus!!)
            }
            currentStatus = lookupStatus!!
        }
    }

    override suspend fun create(name: String, authType: AuthType): Member? {
        var memberStorage = members.find(
            and(
                eq("name", name),
                eq("authType", authType.toString())
            )
        ).firstOrNull()

        if (memberStorage != null) {
            return loadedMembers.get(memberStorage.uid).await()
        }

        val profile = authType.fetcher.fetch(name) ?: return null
        val nextMember = currentStatus.nextMember()
        val nextDataContainer = currentStatus.nextDataContainer()
        val nextBedrockAccountId = currentStatus.nextBedrockAccount()

        val dataContainerStorage = DataContainerStorage(
            ObjectId(),
            nextDataContainer,
            nextMember,
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            mutableMapOf()
        )
        memberStorage = MemberStorage(
            ObjectId(),
            nextMember,
            profile.uuid.toString(),
            name.lowercase(),
            name,
            WhitelistStatus.WHITELISTED.toString(),
            authType.toString(),
            System.currentTimeMillis(),
            null,
            null,
            nextDataContainer,
            if (authType.isBedrock) nextBedrockAccountId else null,
            null,
            mutableListOf(),
            mutableListOf(),
            false
        )

        val member = MemberImpl(this, memberStorage)
        member.dataContainer = DataContainerImpl(member, dataContainerStorage)

        loadedMembers.put(nextMember, CompletableFuture.completedFuture(member))

        if (authType.isBedrock) {
            if (bedrockAccounts.find(eq("xuid", profile.uuid)).firstOrNull() != null) {
                return null
            }

            val bedrockStorage = BedrockAccountStorage(
                ObjectId(),
                nextBedrockAccountId,
                nextMember,
                profile.uuid.xuid,
                name
            )

            member.bedrockAccount = BedrockAccountImpl(member, bedrockStorage)
            updateBedrockAccount(bedrockStorage)
            currentStatus.increaseBedrockAccount()
        }

        updateDataContainer(dataContainerStorage)
        update(member)
        currentStatus.increaseDataContainer()
        currentStatus.increaseMember()

        return member
    }

    override suspend fun lookup(uid: Long): Member? = withContext(Dispatchers.IO) {
        if (!exist(uid)) {
            return@withContext null
        }

        loadedMembers.get(uid).await()
    }

    override suspend fun lookup(uuid: UUID): Member? {
        if (!exist(uuid)) {
            return null
        }

        val storage = members.find(eq("id", uuid.toString())).firstOrNull() ?: return null
        return loadedMembers.get(storage.uid).await()
    }

    override suspend fun lookup(name: String, authType: AuthType): Member? {
        val member = members.find(
            and(
                eq("name", name.lowercase()),
                eq("authType", authType.toString())
            )
        ).firstOrNull() ?: return null
        return loadedMembers.get(member.uid).await()
    }

    override suspend fun exist(uid: Long): Boolean {
        val memberStorage = members.find(eq("uid", uid)).firstOrNull()
        return memberStorage != null
    }

    override suspend fun exist(uuid: UUID): Boolean {
        return members.find(eq("id", uuid.toString())).firstOrNull() != null
    }

    override suspend fun exist(name: String, authType: AuthType): Boolean {
        val member = members.find(
            and(
                eq("name", name.lowercase()),
                eq("authType", authType.toString())
            )
        ).firstOrNull()
        return member != null
    }

    override suspend fun existPunishment(id: Long): Boolean {
        val punishmentStorage = punishments.find(eq("id", id)).firstOrNull()
        return punishmentStorage != null
    }

    override suspend fun existComment(id: Long): Boolean {
        val commentStorage = comments.find(eq("id", id)).firstOrNull()
        return commentStorage != null
    }

    override suspend fun existDataContainer(id: Long): Boolean {
        val dataContainerStorage = dataContainers.find(eq("id", id)).firstOrNull()
        return dataContainerStorage != null
    }

    override suspend fun existBedrockAccount(id: Long): Boolean {
        val bedrockAccountStorage = bedrockAccounts.find(eq("id", id)).firstOrNull()
        return bedrockAccountStorage != null
    }

    override suspend fun isWhitelisted(uid: Long): Boolean {
        return exist(uid) && lookup(uid)!!.isWhitelisted
    }

    override suspend fun isWhitelisted(uuid: UUID): Boolean {
        return exist(uuid) && lookup(uuid)!!.isWhitelisted
    }

    override suspend fun modifier(uid: Long, refresh: Boolean): MemberModifier? {
        if (!exist(uid)) {
            return null
        }

        val member = lookup(uid)

        if (refresh) {
            return member!!.refresh()!!.modifier
        }

        return member!!.modifier
    }

    override suspend fun modifier(uuid: UUID, refresh: Boolean): MemberModifier? {
        if (!exist(uuid)) {
            return null
        }

        val member = lookup(uuid)

        if (refresh) {
            return member!!.refresh()!!.modifier
        }

        return member!!.modifier
    }

    private suspend fun updateMember(member: MemberStorage) {
        members.replaceOne(eq("uid", member.uid), member, updateOptions)
    }

    private suspend fun updatePunishment(punishment: PunishmentStorage) {
        punishments.replaceOne(eq("id", punishment.id), punishment, updateOptions)
    }

    private suspend fun updateComment(comment: CommentStorage, removal: Boolean = false) {
        if (removal) {
            comments.deleteOne(eq("id", comment.id))
            return
        }

        comments.replaceOne(eq("id", comment.id), comment, updateOptions)
    }

    private suspend fun updateDataContainer(dataContainer: DataContainerStorage) {
        dataContainers.replaceOne(eq("id", dataContainer.id), dataContainer, updateOptions)
    }

    private suspend fun updateBedrockAccount(bedrockAccountStorage: BedrockAccountStorage, removal: Boolean = false) {
        if (removal) {
            bedrockAccounts.deleteOne(eq("id", bedrockAccountStorage.id))
            return
        }

        bedrockAccounts.replaceOne(eq("id", bedrockAccountStorage.id), bedrockAccountStorage, updateOptions)
    }

    private suspend fun updateStatus(statusStorage: StatusStorage) {
        status.replaceOne(exists("lastMember"), statusStorage, updateOptions)
    }

    override suspend fun update(member: Member) {
        withContext(Dispatchers.IO) {
            if (loadedMembers.getIfPresent(member.uid) == null) {
                return@withContext
            }

            // 使用 run 包裹，来在不同的部分使用一样的临时变量名
            run {
                val punishments = member.punishmentLogger.historyPunishments.map { it.id }.toMutableList()
                val comments = member.commentRepository.comments.map { it.id }.toMutableList()
                val newStorage = MemberStorage(
                    ObjectId(),
                    member.uid,
                    member.id.toString(),
                    member.name,
                    member.rawName,
                    member.whitelistStatus.toString(),
                    member.authType.toString(),
                    member.createdAt.toEpochMilli(),
                    member.lastJoinedAt?.toEpochMilli(),
                    member.lastQuitedAt?.toEpochMilli(),
                    member.dataContainer.id,
                    member.bedrockAccount?.id,
                    member.bio,
                    punishments,
                    comments,
                    member.isHidden
                )
                updateMember(newStorage)
            }

            run {
                member.punishmentLogger.historyPunishments.forEach {
                    val storage = PunishmentStorage(
                        ObjectId(),
                        it.id,
                        it.type.toString(),
                        it.time.toEpochMilli(),
                        it.belongs.uid,
                        it.isRevoked,
                        it.executor().uid
                    )
                    updatePunishment(storage)
                }
            }

            run {
                val repo = member.commentRepository as AbstractCommentRepository
                repo.comments.forEach {
                    val storage = CommentStorage(
                        ObjectId(),
                        it.id,
                        it.createdAt.toEpochMilli(),
                        it.creator.uid,
                        it.content,
                        it.isModified
                    )
                    updateComment(storage)
                    repo.dirtyComments.forEach { dirty -> updateComment(dirty, true) }
                    repo.dirtyComments.clear()
                }
            }

            run {
                val container = member.dataContainer as AbstractDataContainer
                val storage = DataContainerStorage(
                    ObjectId(),
                    container.id,
                    container.owner.uid,
                    container.createdAt.toEpochMilli(),
                    container.lastModifiedAt.toEpochMilli(),
                    container.contents.toMutableMap()
                )
                updateDataContainer(storage)
            }

            run {
                val abs = member as AbstractMember

                abs.dirtyBedrockAccounts.forEach { dirty -> updateBedrockAccount(dirty, true) }
                abs.dirtyBedrockAccounts.clear()

                if (abs.bedrockAccount != null) {
                    val account = member.bedrockAccount as AbstractBedrockAccount
                    val storage = BedrockAccountStorage(
                        ObjectId(),
                        account.id,
                        account.linkedWith.uid,
                        account.xuid,
                        account.gamertag
                    )
                    updateBedrockAccount(storage)
                }
            }

            updateStatus(currentStatus)
        }
    }

    override suspend fun update(uid: Long) {
        loadedMembers.asMap().values.firstOrNull { it.await()?.uid == uid } ?: return
        update(loadedMembers.get(uid).await()!!)
    }

    override suspend fun update(uuid: UUID) {
        val member = loadedMembers.asMap().values.firstOrNull { it.await()?.id == uuid } ?: return
        update(member.await()!!)
    }

    override suspend fun refresh(member: Member): Member? {
        if (!exist(member.uid)) {
            return null
        }

        currentStatus = status.find(exists("lastMember")).firstOrNull()!!
        loadedMembers.synchronous().invalidate(member.uid)
        return lookup(member.uid)
    }

    override suspend fun refresh(uid: Long): Member? {
        if (!exist(uid)) {
            return null
        }

        currentStatus = status.find(exists("lastMember")).firstOrNull()!!
        loadedMembers.synchronous().invalidate(uid)
        return lookup(uid)
    }

    override suspend fun refresh(uuid: UUID): Member? {
        if (!exist(uuid)) {
            return null
        }

        val member = lookup(uuid)
        currentStatus = status.find(exists("lastMember")).firstOrNull()!!
        loadedMembers.synchronous().invalidate(member!!.uid)
        return lookup(uuid)
    }

}