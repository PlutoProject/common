package ink.pmc.utils.platform

import java.util.concurrent.Executor

@Suppress("UNUSED")
val tpsLast1Minute: Double
    get() = 20.0

@Suppress("UNUSED")
val tpsLast5Minute: Double
    get() = 20.0

@Suppress("UNUSED")
val tpsLast15Minute: Double
    get() = 20.0

@Suppress("UNUSED")
val currentMSPT: Double
    get() = 0.0

@Suppress("UNUSED")
lateinit var serverExecutor: Executor