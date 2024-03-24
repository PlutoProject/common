package ink.pmc.common.member

import com.velocitypowered.api.proxy.connection.Player

fun Player.startPlay() {
    PlayTimeLogger.start(this)
}

fun Player.stopPlay(): Long {
    return PlayTimeLogger.stop(this)
}