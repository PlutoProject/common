package ink.pmc.utils.bedrock

import ink.pmc.utils.chat.sendActionBar
import ink.pmc.utils.chat.sendMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

private val legacySerializer = LegacyComponentSerializer.legacy('§')

@Suppress("UNUSED")
val Player.isBedrock: Boolean
    get() = isFloodgatePlayer(this.uniqueId)

@Suppress("UNUSED")
fun Player.fallback(original: Component, bedrock: Component): Component {
    if (this.isBedrock) {
        return bedrock
    }

    return original
}

@Suppress("UNUSED")
fun Player.sendMessage(component: Component, fallback: Component) {
    this.sendMessage(this.fallback(component, fallback))
}

@Suppress("UNUSED")
fun Player.sendActionBar(component: Component, fallback: Component) {
    this.sendActionBar(this.fallback(component, fallback))
}

@Suppress("UNUSED")
fun Player.kick(component: Component, fallback: Component) {
    this.kickPlayer(legacySerializer.serialize(this.fallback(component, fallback)))
}