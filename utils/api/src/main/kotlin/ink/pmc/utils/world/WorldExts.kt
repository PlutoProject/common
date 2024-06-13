package ink.pmc.utils.world

import org.bukkit.World

val World.playerCount: Int
    get() = this.players.size

val World.entityCount: Int
    get() = this.entities.size

val World.chunkCount: Int
    get() = this.loadedChunks.size