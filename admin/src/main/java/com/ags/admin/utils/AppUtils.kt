package com.ags.admin.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object AppUtils {

    fun formatTimestamp(timestamp: Long?): String {
        return if (timestamp != null) {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "Unknown time"
        }
    }

    fun convertTimeMillisToHours(timeMillis: Long?): String {
        return if (timeMillis != null) {
            val hours = TimeUnit.MILLISECONDS.toHours(timeMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis) % 60
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            "Unknown time"
        }
    }
}