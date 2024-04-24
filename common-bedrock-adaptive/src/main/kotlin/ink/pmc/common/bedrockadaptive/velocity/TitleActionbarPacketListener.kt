package ink.pmc.common.bedrockadaptive.velocity

import com.velocitypowered.proxy.protocol.packet.title.TitleActionbarPacket
import dev.simplix.protocolize.api.Direction
import dev.simplix.protocolize.api.listener.AbstractPacketListener
import dev.simplix.protocolize.api.listener.PacketReceiveEvent
import dev.simplix.protocolize.api.listener.PacketSendEvent
import ink.pmc.common.bedrockadaptive.utils.replaceFallbackColor
import ink.pmc.common.member.api.session.SessionService

object TitleActionbarPacketListener : AbstractPacketListener<TitleActionbarPacket>(
    TitleActionbarPacket::class.java,
    Direction.UPSTREAM,
    0
) {

    override fun packetReceive(event: PacketReceiveEvent<TitleActionbarPacket>) {
    }

    override fun packetSend(event: PacketSendEvent<TitleActionbarPacket>) {
        if (!SessionService.isBedrockSession(event.player().uniqueId())) {
            return
        }

        replaceFallbackColor(event.packet())
    }

}