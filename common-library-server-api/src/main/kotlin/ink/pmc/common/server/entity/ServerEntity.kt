package ink.pmc.common.server.entity

import ink.pmc.common.server.Server
import ink.pmc.common.server.world.ServerLocation
import ink.pmc.common.server.world.ServerWorld
import net.kyori.adventure.text.Component
import java.util.*

interface ServerEntity {

    val uniqueID: UUID
    val server: Server
    val status: EntityStatus
    val name: String
    var customName: Component?
        get() = operator.getCustomName(this)
        set(value) {
            if (value == null) {
                return
            }

            operator.setCustomName(this, value)
        }
    val operator: EntityOperator

    fun teleport(location: ServerLocation) {
        operator.teleport(this, location)
    }

    fun teleport(world: ServerWorld, x: Double, y: Double, z: Double)

}