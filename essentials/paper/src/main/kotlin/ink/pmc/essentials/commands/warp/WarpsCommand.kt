package ink.pmc.essentials.commands.warp

import cafe.adriel.voyager.navigator.Navigator
import ink.pmc.essentials.Cm
import ink.pmc.essentials.VIEWER_PAGING_SOUND
import ink.pmc.essentials.commands.checkPlayer
import ink.pmc.essentials.screens.WarpViewerScreen
import ink.pmc.interactive.inventory.canvas.inv
import ink.pmc.utils.annotation.Command
import ink.pmc.utils.dsl.cloud.invoke
import ink.pmc.utils.dsl.cloud.sender

@Command("warps")
@Suppress("UNUSED")
fun Cm.warps(aliases: Array<String>) {
    this("warps", *aliases) {
        permission("essentials.warps")
        handler {
            checkPlayer(sender.sender) {
                inv { Navigator(WarpViewerScreen(this)) }
                playSound(VIEWER_PAGING_SOUND)
            }
        }
    }
}