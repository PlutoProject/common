package ink.pmc.utils

@Suppress("UNUSED")
fun isInDebugMode(): Boolean {
    return System.getProperty("PLUTO_DEBUG_MODE") != null
}