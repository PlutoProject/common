package ink.pmc.common.misc

import ink.pmc.common.utils.NON_PLAYER
import ink.pmc.common.utils.execute
import org.bukkit.entity.Player

val suicideCommand = commandManager.commandBuilder("suicide")
    .handler {
        val sender = it.sender()

        if (sender !is Player) {
            sender.sendMessage(NON_PLAYER)
            return@handler
        }

        sender.execute(plugin) {
            sender.health = 0.0
            sender.sendMessage(SUICIDE)
        }
    }