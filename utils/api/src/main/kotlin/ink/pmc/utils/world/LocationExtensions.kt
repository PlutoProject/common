package ink.pmc.utils.world

import org.bukkit.Location

fun Location.toBlockLocation(): Location {
    return Location(world, blockX.toDouble(), blockY.toDouble(), blockZ.toDouble())
}

fun toRawLocation(location: Location): Location {
    val rawLocation = location.toBlockLocation()

    rawLocation.yaw = 0F
    rawLocation.pitch = 0F

    return rawLocation
}

@Suppress("UNUSED")
val Location.loc2D: Loc2D
    get() = Loc2D(this)

@Suppress("UNUSED")
val Location.rawLoc2D: Loc2D
    get() = Loc2D(this.rawLocation)
val Location.rawLocation: Location
    get() = toRawLocation(this)