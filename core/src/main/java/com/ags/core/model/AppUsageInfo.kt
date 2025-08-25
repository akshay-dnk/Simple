package com.ags.core.model

data class AppUsageInfo(
    val packageName: String = "",
    val appName: String = "",
    val lastTimeUsed: Long = 0L,
    val totalTimeVisible: Long = 0L,
)