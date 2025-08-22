package com.ags.admin.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppUtils {

    fun formatTimestamp(timestamp: Long?): String {
        return if (timestamp != null) {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "Unknown time"
        }
    }

}