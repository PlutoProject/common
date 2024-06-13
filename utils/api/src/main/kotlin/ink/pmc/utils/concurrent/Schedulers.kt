package ink.pmc.utils.concurrent

import ink.pmc.utils.platform.paperUtilsPlugin
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

@Suppress("UNUSED")
fun Entity.scheduler(plugin: JavaPlugin, retired: () -> Unit = {}, delay: Long = 0L, task: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            task()
        }
    }.runTaskLater(plugin, delay)
}

@Suppress("UNUSED")
fun globalRegionScheduler(plugin: JavaPlugin, task: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            task()
        }
    }.runTask(plugin)
}

@Suppress("UNUSED")
fun regionScheduler(plugin: JavaPlugin, location: Location, task: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            task()
        }
    }.runTask(plugin)
}

@Suppress("UNUSED")
fun regionScheduler(plugin: JavaPlugin, chunk: Chunk, task: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            task()
        }
    }.runTask(plugin)
}

@Suppress("UNUSED")
fun Entity.scheduler(retired: () -> Unit = {}, delay: Long = 0L, task: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            task()
        }
    }.runTaskLater(paperUtilsPlugin, delay)
}

@Suppress("UNUSED")
fun globalRegionScheduler(task: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            task()
        }
    }.runTask(paperUtilsPlugin)
}

@Suppress("UNUSED")
fun regionScheduler(location: Location, task: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            task()
        }
    }.runTask(paperUtilsPlugin)
}

@Suppress("UNUSED")
fun regionScheduler(chunk: Chunk, task: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            task()
        }
    }.runTask(paperUtilsPlugin)
}

fun Chunk.scheduler(task: () -> Unit) {
    regionScheduler(this, task)
}

fun Location.scheduler(task: () -> Unit) {
    regionScheduler(this, task)
}