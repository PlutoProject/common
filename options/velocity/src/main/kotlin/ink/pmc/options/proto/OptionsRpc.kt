package ink.pmc.options.proto

import com.google.protobuf.Empty
import ink.pmc.options.api.OptionsManager
import ink.pmc.options.proto.OptionsUpdateNotifyOuterClass.OptionsUpdateNotify
import ink.pmc.utils.concurrent.submitAsyncIO
import ink.pmc.utils.player.uuid
import ink.pmc.utils.proto.empty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*

object OptionsRpc : OptionsRpcGrpcKt.OptionsRpcCoroutineImplBase() {
    private val id: UUID = UUID.randomUUID()
    private val broadcast = MutableSharedFlow<OptionsUpdateNotify>()

    override suspend fun notifyOptionsUpdate(request: OptionsUpdateNotify): Empty {
        val player = request.player.uuid
        if (OptionsManager.isPlayerLoaded(player)) {
            OptionsManager.reloadOptions(player)
        }
        broadcast.emit(request)
        return empty
    }

    override fun monitorOptionsUpdate(request: Empty): Flow<OptionsUpdateNotify> {
        return broadcast
    }

    fun notifyBackendContainerUpdate(player: UUID) {
        submitAsyncIO {
            broadcast.emit(optionsUpdateNotify {
                serverId = id.toString()
                this.player = player.toString()
            })
        }
    }
}