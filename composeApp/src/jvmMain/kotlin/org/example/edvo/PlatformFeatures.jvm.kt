package org.example.edvo

// JVM implementation
actual fun setScreenProtection(enabled: Boolean) {
    // No-op for Desktop usually, or specific OS logic.
}

actual fun formatShortDate(epochMillis: Long): String {
    val date = java.util.Date(epochMillis)
    val formatter = java.text.SimpleDateFormat("MMM dd", java.util.Locale.US)
    return formatter.format(date)
}
