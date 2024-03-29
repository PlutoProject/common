package ink.pmc.common.utils.visual

import net.kyori.adventure.text.format.TextColor
import java.awt.Color

@Suppress("UNUSED")
fun Color.toTextColor(): TextColor {
    return TextColor.color(red, green, blue)
}