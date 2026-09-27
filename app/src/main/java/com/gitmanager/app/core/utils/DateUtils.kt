package com.gitmanager.app.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    fun formatRelativeTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Recently"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(isoString) ?: return isoString
            val now = System.currentTimeMillis()
            val diff = now - date.time

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                days > 365 -> "${days / 365}y ago"
                days > 30 -> "${days / 30}mo ago"
                days > 0 -> "${days}d ago"
                hours > 0 -> "${hours}h ago"
                minutes > 0 -> "${minutes}m ago"
                else -> "Just now"
            }
        } catch (e: Exception) {
            isoString.take(10)
        }
    }

    fun formatDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val inputSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = inputSdf.parse(isoString) ?: return isoString
            val outputSdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            outputSdf.format(date)
        } catch (e: Exception) {
            isoString.take(10)
        }
    }
}
