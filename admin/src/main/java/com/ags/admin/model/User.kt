package com.ags.admin.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val profileUrl: String = ""
) : Parcelable
