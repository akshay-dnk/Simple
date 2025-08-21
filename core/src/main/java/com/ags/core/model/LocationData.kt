package com.ags.core.model

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "N/A",
    val timestamp: Long = System.currentTimeMillis()
)