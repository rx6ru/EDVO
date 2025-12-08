package org.example.edvo.util

expect object DateUtil {
    fun formatShort(timestamp: Long): String
    fun formatFull(timestamp: Long): String
}
