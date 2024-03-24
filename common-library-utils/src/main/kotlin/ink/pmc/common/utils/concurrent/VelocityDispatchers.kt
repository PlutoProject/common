package ink.pmc.common.utils.concurrent

import ink.pmc.common.utils.platform.velocityProxyServer
import ink.pmc.common.utils.platform.velocityUtilsPlugin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

val velocityDispatcher: CoroutineDispatcher = object : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        velocityProxyServer.scheduler().buildTask(velocityUtilsPlugin) {
            block.run()
        }.schedule()
    }
}