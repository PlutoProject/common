package ink.pmc.common.member.data

import ink.pmc.common.member.api.data.DataContainer
import java.time.LocalDateTime
import java.util.*

class DataContainerImpl : DataContainer {

    override val id: Long
        get() = TODO("Not yet implemented")
    override val owner: UUID
        get() = TODO("Not yet implemented")
    override val createdAt: LocalDateTime
        get() = TODO("Not yet implemented")
    override val lastModifiedAt: LocalDateTime?
        get() = TODO("Not yet implemented")
    override val contents: Map<String, Any>
        get() = TODO("Not yet implemented")

    override fun set(key: String, value: Any) {
        TODO("Not yet implemented")
    }

    override fun <T> get(key: String): T? {
        TODO("Not yet implemented")
    }

    override fun getString(key: String): String? {
        TODO("Not yet implemented")
    }

    override fun getByte(key: String): Byte? {
        TODO("Not yet implemented")
    }

    override fun getShort(key: String): Short? {
        TODO("Not yet implemented")
    }

    override fun getInt(key: String): Int? {
        TODO("Not yet implemented")
    }

    override fun getLong(key: String): Long? {
        TODO("Not yet implemented")
    }

    override fun getFloat(key: String): Float? {
        TODO("Not yet implemented")
    }

    override fun getDouble(key: String): Double? {
        TODO("Not yet implemented")
    }

    override fun getChar(key: String): Char? {
        TODO("Not yet implemented")
    }

    override fun getBoolean(key: String): Boolean {
        TODO("Not yet implemented")
    }

}