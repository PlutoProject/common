package ink.pmc.utils.chat

import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.command.CommandSender

lateinit var adventure: BukkitAudiences

fun CommandSender.sendMessage(component: Component) {
    adventure.sender(this).sendMessage(component)
}

fun CommandSender.showTitle(title: Title) {
    adventure.sender(this).showTitle(title)
}

fun CommandSender.sendActionBar(component: Component) {
    adventure.sender(this).sendActionBar(component)
}