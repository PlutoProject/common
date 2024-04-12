package ink.pmc.common.refactor.member.storage

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class MemberStorage(
    @BsonId val objectId: ObjectId,
    val uid: Long,
    val id: String,
    var name: String,
    var whitelistStatus: String,
    val authType: String,
    var createdAt: Long,
    var lastJoinedAt: Long?,
    val dataContainer: Long,
    var bedrockAccount: Long?,
    var bio: String?
)