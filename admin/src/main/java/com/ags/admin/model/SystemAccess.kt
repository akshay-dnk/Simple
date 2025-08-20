package com.ags.admin.model

data class SystemAccess(
    val type: SystemFeatureType,
    val title: String,
    val description: String,
    val iconRes: Int,
    val permissionName: String,
    val enabled: Boolean = false
)
