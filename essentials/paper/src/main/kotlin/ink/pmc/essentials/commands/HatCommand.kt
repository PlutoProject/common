package ink.pmc.essentials.commands

import ink.pmc.essentials.*
import ink.pmc.utils.chat.NO_PERMISSON
import ink.pmc.utils.chat.replace
import ink.pmc.utils.concurrent.sync
import ink.pmc.utils.dsl.cloud.invoke
import ink.pmc.utils.dsl.cloud.sender
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.bukkit.parser.PlayerParser
import kotlin.jvm.optionals.getOrNull

@Command("hat")
@Suppress("UNUSED")
fun Cm.hat(aliases: Array<String>) {
    this("hat", *aliases) {
        permission("essentials.hat")
        optional("player", PlayerParser.playerParser())
        handler {
            checkPlayer(sender.sender) {
                val argPlayer = optional<Player>("player").getOrNull()

                if (handItem.type == Material.AIR) {
                    sendMessage(COMMAND_HAT_FAILED_EMPTY_HAND)
                    return@checkPlayer
                }

                if (argPlayer != null) {
                    if (!hasPermission("essentials.hat.other")) {
                        sendMessage(NO_PERMISSON)
                        return@checkPlayer
                    }

                    if (argPlayer.hatItem != null) {
                        sendMessage(COMMAND_HAT_FAILED_EXISTED_OTHER)
                        return@checkPlayer
                    }

                    argPlayer.hat(handItem)
                    clearHand()
                    sendMessage(COMMAND_HAT_SUCCEED_OTHER.replace("<player>", argPlayer.name))
                    return@checkPlayer
                }

                if (hatItem != null) {
                    sendMessage(COMMAND_HAT_FAILED_EXISTED)
                    return@checkPlayer
                }

                hat(handItem)
                clearHand()
                sendMessage(COMMAND_HAT_SUCCEED)
            }
        }
    }
}

private var Player.handItem: ItemStack
    set(value) = inventory.setItemInMainHand(value)
    get() = inventory.itemInMainHand

private val Player.hatItem: ItemStack?
    get() = inventory.helmet

private suspend fun Player.hat(item: ItemStack) {
    sync {
        inventory.helmet = item
    }
}

private suspend fun Player.clearHand() {
    sync {
        handItem = ItemStack(Material.AIR)
    }
}