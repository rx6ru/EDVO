package org.example.edvo

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.system.exitProcess

import platform.Foundation.NSBundle

actual fun setScreenProtection(enabled: Boolean) {
    // iOS Screen Protection (often handled in AppDelegate or SwiftUI view modifiers, hard to do from shared function without context).
    // Leaving no-op for now as main app logic is in Swift for that.
}

actual fun killApp() {
    // iOS doesn't allow programmatic app termination in the same way
    // Using exit(0) - note: Apple may reject apps that call exit() in production
    exitProcess(0)
}

actual fun getAppVersion(): String {
    return NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "0.5.1"
}
actual fun getUpdateCachePath(): String? = null
actual fun installApk(path: String) {}
actual fun saveFile(path: String, data: ByteArray) {}

actual fun formatShortDate(epochMillis: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0)
    val formatter = NSDateFormatter()
    formatter.dateFormat = "MMM dd"
    // formatter.locale = NSLocale.currentLocale // Optional, defaults to system
    return formatter.stringFromDate(date)
}
