package ink.pmc.hypervisor

import org.bukkit.event.Listener

@Suppress("UNUSED")
object DuplicatedUuidDetector : Listener {
    // Not in use
    /*    fun entityMoveEvent(event: EntityMoveEvent) {
            val entity = event.entity

            if (entity.type == EntityType.FALLING_BLOCK) {
                return
            }

            val uuid = event.entity.uniqueId

            if (!paper.worlds.any { it.entities.any { e -> e.uniqueId == uuid } }) {
                return
            }

            event.isCancelled = true
            entity.remove()

            serverLogger.warning("Removed duplicated uuid entity")
        }*/

}