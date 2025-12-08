package org.example.edvo.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual object DateUtil {
    actual fun formatShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd", Locale.US)
        return sdf.format(Date(timestamp))
    }
    actual fun formatFull(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US)
        return sdf.format(Date(timestamp))
    }
}
