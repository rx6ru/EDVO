package org.example.edvo

import kotlin.system.exitProcess

// JVM implementation
actual fun setScreenProtection(enabled: Boolean) {
    // No-op for Desktop usually, or specific OS logic.
}

actual fun killApp() {
    exitProcess(0)
}

actual fun getAppVersion(): String = "0.4.0"
actual fun getUpdateCachePath(): String? = null
actual fun installApk(path: String) {}
actual fun saveFile(path: String, data: ByteArray) {
    java.io.File(path).writeBytes(data)
}
