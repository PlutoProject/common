package ink.pmc.common.member.api

import ink.pmc.common.utils.visual.mochaMaroon
import ink.pmc.common.utils.visual.mochaPeach
import ink.pmc.common.utils.visual.mochaYellow
import net.kyori.adventure.text.Component

@Suppress("UNUSED")
val BAN_DISPLAY
    get() = Component.text("封禁").color(mochaMaroon)

@Suppress("UNUSED")
val WARN_A_DISPLAY
    get() = Component.text("A 类警告").color(mochaPeach)

@Suppress("UNUSED")
val WARN_B_DISPLAY
    get() = Component.text("B 类警告").color(mochaYellow)