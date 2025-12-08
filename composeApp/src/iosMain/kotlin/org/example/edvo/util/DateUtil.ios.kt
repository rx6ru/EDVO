package org.example.edvo.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual object DateUtil {
    actual fun formatShort(timestamp: Long): String {
        val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
        val formatter = NSDateFormatter()
        formatter.dateFormat = "MMM dd"
        return formatter.stringFromDate(date)
    }
    actual fun formatFull(timestamp: Long): String {
        val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
        val formatter = NSDateFormatter()
        formatter.dateFormat = "MMM dd, yyyy • HH:mm"
        return formatter.stringFromDate(date)
    }
}
