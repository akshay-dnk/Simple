package com.ags.core.model

data class AppUsageDateInfo(
    val date: String = "",
    val timestamp: Long = 0L,       // upload timestamp
    val totalApps: Int = 0,         // number of apps tracked
    val totalUsageTime: Long = 0L   // total usage time of all apps (ms)
)