package ink.pmc.utils.player

import net.minecraft.server.v1_8_R3.Packet
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player

fun Player.sendPacket(packet: Packet<*>) {
    this as CraftPlayer
    handle.playerConnection.sendPacket(packet)
}