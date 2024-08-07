package ink.pmc.interactive.inventory.canvas

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import ink.pmc.utils.concurrent.submitSync
import ink.pmc.utils.platform.paperUtilsPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.wesjd.anvilgui.AnvilGUI
import net.wesjd.anvilgui.AnvilGUI.ResponseAction
import net.wesjd.anvilgui.AnvilGUI.StateSnapshot
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

typealias CloseListener = (StateSnapshot) -> Unit
typealias ClickListener = (Int, StateSnapshot) -> List<ResponseAction>

@Composable
@Suppress("FunctionName")
fun Anvil(
    viewer: Player,
    title: Component,
    left: ItemStack? = null,
    right: ItemStack? = null,
    output: ItemStack? = null,
    text: String = "",
    onClose: CloseListener = {},
    onClick: ClickListener = { _, _ -> listOf() },
) {
    var textState by rememberSaveable { mutableStateOf(text) }
    remember(title, left, right, output) {
        submitSync {
            AnvilGUI.Builder()
                .plugin(paperUtilsPlugin)
                .jsonTitle(GsonComponentSerializer.gson().serialize(title))
                .text(textState)
                .apply {
                    if (left != null) itemLeft(left)
                    if (right != null) itemRight(right)
                    if (output != null) itemOutput(output)
                }
                .onClose {
                    textState = it.text
                    onClose(it)
                }
                .onClick { i, s ->
                    textState = s.text
                    onClick(i, s)
                }
                .open(viewer)
        }
    }
}