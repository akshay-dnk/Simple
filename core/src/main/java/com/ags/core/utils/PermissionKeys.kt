package com.ags.core.utils

import android.Manifest

object PermissionKeys {

    const val READ_CONTACTS = "read_contacts"
    const val CAMERA = "camera"
    const val RECORD_AUDIO = "record_audio"
    const val FINE_LOCATION = "fine_location"
    const val RECEIVE_SMS = "receive_sms"
    const val READ_SMS = "read_sms"

    fun toKey(permission: String): String = when (permission) {
        Manifest.permission.READ_CONTACTS -> READ_CONTACTS
        Manifest.permission.CAMERA -> CAMERA
        Manifest.permission.RECORD_AUDIO -> RECORD_AUDIO
        Manifest.permission.ACCESS_FINE_LOCATION -> FINE_LOCATION
        Manifest.permission.RECEIVE_SMS -> RECEIVE_SMS
        Manifest.permission.READ_SMS -> READ_SMS
        else -> permission.replace(".", "_")
    }
}