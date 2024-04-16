package ink.pmc.common.member

import ink.pmc.common.member.api.Member
import ink.pmc.common.member.storage.BedrockAccountStorage
import ink.pmc.common.member.storage.MemberStorage

abstract class AbstractMember : Member {

    abstract val storage: MemberStorage
    val dirtyBedrockAccounts = mutableListOf<BedrockAccountStorage>()

    override fun equals(other: Any?): Boolean {
        if (other !is Member) {
            return false
        }

        return other.uid == uid
    }

    override fun hashCode(): Int {
        return storage.hashCode()
    }

}