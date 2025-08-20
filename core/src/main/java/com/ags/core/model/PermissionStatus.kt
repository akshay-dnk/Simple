package com.ags.core.model

data class PermissionStatus(
    val permissionName: String = "",
    val granted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)