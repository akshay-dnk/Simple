package com.ags.core.utils

import android.Manifest
import com.ags.core.model.SystemFeatureType

object PermissionKeys {

    const val READ_CONTACTS = "read_contacts"
    const val CAMERA = "camera"
    const val RECORD_AUDIO = "record_audio"
    const val FINE_LOCATION = "fine_location"
    const val READ_SMS = "read_sms"
    const val APP_USAGE = "app_usage"

    fun toKey(permission: String): String = when (permission) {
        Manifest.permission.READ_CONTACTS -> READ_CONTACTS
        Manifest.permission.CAMERA -> CAMERA
        Manifest.permission.RECORD_AUDIO -> RECORD_AUDIO
        Manifest.permission.ACCESS_FINE_LOCATION -> FINE_LOCATION
        Manifest.permission.READ_SMS -> READ_SMS
        Manifest.permission.PACKAGE_USAGE_STATS -> APP_USAGE
        else -> permission.replace(".", "_")
    }

    fun fromKey(key: SystemFeatureType): String = when (key) {
        SystemFeatureType.READ_CONTACTS -> Manifest.permission.READ_CONTACTS
        SystemFeatureType.LIVE_CAMERA -> Manifest.permission.CAMERA
        SystemFeatureType.RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
        SystemFeatureType.FINE_LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
        SystemFeatureType.READ_SMS -> Manifest.permission.READ_SMS
        SystemFeatureType.APP_USAGE -> Manifest.permission.PACKAGE_USAGE_STATS
    }

}