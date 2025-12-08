package org.example.edvo

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun setScreenProtection(enabled: Boolean) {
    // iOS Screen Protection (often handled in AppDelegate or SwiftUI view modifiers, hard to do from shared function without context).
    // Leaving no-op for now as main app logic is in Swift for that.
}

actual fun formatShortDate(epochMillis: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0)
    val formatter = NSDateFormatter()
    formatter.dateFormat = "MMM dd"
    // formatter.locale = NSLocale.currentLocale // Optional, defaults to system
    return formatter.stringFromDate(date)
}
