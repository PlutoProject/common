package ink.pmc.common.utils.concurrent

import kotlinx.coroutines.*
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity

@Suppress("UNUSED")
@OptIn(DelicateCoroutinesApi::class)
fun submitSync(block: suspend CoroutineScope.() -> Unit): Job = GlobalScope.launch(mainThreadDispatcher) { block() }

@Suppress("UNUSED")
@OptIn(DelicateCoroutinesApi::class)
@JvmName("syncEntity")
fun submitSync(entity: Entity, block: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch(entity.dispatcher) { block() }

@Suppress("UNUSED")
@OptIn(DelicateCoroutinesApi::class)
@JvmName("syncChunk")
fun submitSync(chunk: Chunk, block: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch(chunk.dispatcher) { block() }

@Suppress("UNUSED")
@OptIn(DelicateCoroutinesApi::class)
@JvmName("syncLocation")
fun submitSync(location: Location, block: suspend CoroutineScope.() -> Unit) =
    GlobalScope.launch(location.dispatcher) { block() }

@Suppress("UNUSED")
suspend fun sync(block: suspend CoroutineScope.() -> Unit) = withContext(mainThreadDispatcher) { block() }

@Suppress("UNUSED")
@JvmName("syncEntitySuspend")
suspend fun sync(entity: Entity, block: suspend CoroutineScope.() -> Unit) =
    withContext(entity.dispatcher) { block() }

@Suppress("UNUSED")
@JvmName("syncChunkSuspend")
suspend fun sync(chunk: Chunk, block: suspend CoroutineScope.() -> Unit) =
    withContext(chunk.dispatcher) { block() }

@Suppress("UNUSED")
@JvmName("syncLocationSuspend")
suspend fun sync(location: Location, block: suspend CoroutineScope.() -> Unit) =
    withContext(location.dispatcher) { block() }

@Suppress("UNUSED")
@OptIn(DelicateCoroutinesApi::class)
@JvmName("entitySync")
fun Entity.submitSync(block: suspend CoroutineScope.() -> Unit): Job = GlobalScope.launch(this.dispatcher) { block() }

@Suppress("UNUSED")
@OptIn(DelicateCoroutinesApi::class)
@JvmName("chunkSync")
fun Chunk.submitSync(block: suspend CoroutineScope.() -> Unit): Job = GlobalScope.launch(this.dispatcher) { block() }

@Suppress("UNUSED")
@OptIn(DelicateCoroutinesApi::class)
@JvmName("locationSync")
fun Location.submitSync(block: suspend CoroutineScope.() -> Unit): Job = GlobalScope.launch(this.dispatcher) { block() }

@Suppress("UNUSED")
@JvmName("entitySyncSuspend")
suspend fun Entity.sync(block: suspend CoroutineScope.() -> Unit) = withContext(this.dispatcher) { block() }

@Suppress("UNUSED")
@JvmName("chunkSyncSuspend")
suspend fun Chunk.sync(block: suspend CoroutineScope.() -> Unit) = withContext(this.dispatcher) { block() }

@Suppress("UNUSED")
@JvmName("locationSyncSuspend")
suspend fun Location.sync(block: suspend CoroutineScope.() -> Unit) = withContext(this.dispatcher) { block() }