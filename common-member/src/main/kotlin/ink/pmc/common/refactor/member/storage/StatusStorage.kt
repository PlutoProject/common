package ink.pmc.common.refactor.member.storage

import ink.pmc.common.refactor.member.UID_START
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class StatusStorage(
    @BsonId val objectId: ObjectId,
    var lastMember: Long,
    var lastPunishment: Long,
    var lastComment: Long,
    var lastDataContainer: Long,
    var lastBedrockAccount: Long
) {

    fun nextMember(): Long {
        if (lastMember == -1L) {
            return UID_START
        }

        return lastMember + 1
    }

    fun nextPunishment(): Long {
        if (lastPunishment == -1L) {
            return 0
        }

        return lastPunishment + 1
    }

    fun nextComment(): Long {
        if (lastComment == -1L) {
            return 0
        }

        return lastComment + 1
    }

    fun nextDataContainer(): Long {
        if (lastDataContainer == -1L) {
            return 0
        }

        return lastDataContainer + 1
    }

    fun nextBedrockAccount(): Long {
        if (lastBedrockAccount == -1L) {
            return 0
        }

        return lastBedrockAccount + 1
    }

}