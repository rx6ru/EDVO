package org.example.edvo

import kotlin.system.exitProcess

// JVM implementation
actual fun setScreenProtection(enabled: Boolean) {
    // No-op for Desktop usually, or specific OS logic.
}

actual fun killApp() {
    exitProcess(0)
}
