package ink.pmc.common.server.impl.velocity

import ink.pmc.common.server.Server
import ink.pmc.common.server.impl.ProxyServerService
import ink.pmc.common.server.impl.message.ChannelImpl
import ink.pmc.common.server.impl.velocity.message.VelocityMessageManager
import ink.pmc.common.server.impl.velocity.network.VelocityInboundHandler
import ink.pmc.common.server.impl.velocity.proxy.VelocityNetwork
import ink.pmc.common.server.impl.velocity.proxy.VelocityProxy
import ink.pmc.common.server.message.Channel
import ink.pmc.common.server.message.MessageManager
import ink.pmc.common.server.network.Network
import ink.pmc.common.server.network.Proxy
import ink.pmc.common.server.pluginLogger
import ink.pmc.common.utils.concurrent.submitAsync
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.delay
import java.util.*
import kotlin.time.Duration

@Suppress("UNUSED")
class VelocityServerService(
    private val host: String,
    private val port: Int,
    private val id: Long,
    private val name: String
) : ProxyServerService() {

    val connectedChannelIdsToChannelMap = mutableMapOf<Long, SocketChannel>()
    val verifiedClientIdsToChannelMap = mutableMapOf<UUID, SocketChannel>()
    val channelHandler = VelocityInboundHandler(this)
    private val channelInitializer = object : ChannelInitializer<SocketChannel>() {
        override fun initChannel(p0: SocketChannel) {
            val address = p0.remoteAddress()
            val id = p0.id().asLongText().toLong()
            connectedChannelIdsToChannelMap[id] = p0
            p0.pipeline().addLast(channelHandler)

            pluginLogger.info("Channel ${p0.id()} connected.")
            pluginLogger.info("Address: ${address.address}:${address.port}")
            pluginLogger.info("Waiting 10s for this channel to verify.")

            submitAsync {
                delay(Duration.parse("10s"))

                if (verifiedClientIdsToChannelMap.values.contains(p0)) {
                    return@submitAsync
                }

                pluginLogger.info("Channel ${p0.id()} didn't verify in 10s, disconnecting.")
                p0.disconnect().sync().await()
            }
        }
    }

    private fun init() {
        try {
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(channelInitializer)

            val future = bootstrap.bind(host, port).sync()
            future.sync().await()

            pluginLogger.info("Server started at $host:$port.")
        } catch (e: Exception) {
            pluginLogger.severe("Failed to start server!")
            close()
            throw RuntimeException(e)
        }
    }

    init {
        init()
    }

    override val server: Server = VelocityProxy(id, name)
    override val network: Network = VelocityNetwork(server as Proxy)
    override val messageManager: MessageManager = VelocityMessageManager(this)
    override var channel: Channel = ChannelImpl(messageManager, "_internalChannel")

}