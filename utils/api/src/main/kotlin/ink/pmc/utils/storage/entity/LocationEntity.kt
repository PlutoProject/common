package ink.pmc.utils.storage.entity

import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location

val Location.entity: LocationEntity
    get() = LocationEntity(
        world.name,
        x,
        y,
        z,
        yaw,
        pitch,
    )

@Serializable
@Suppress("UNUSED")
data class LocationEntity(
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
) {

    val location: Location?
        get() {
            return Location(
                Bukkit.getWorld(world) ?: return null,
                x,
                y,
                z,
                yaw,
                pitch,
            )
        }
}