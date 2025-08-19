package com.ags.admin.model

data class User(
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val profileUrl: String = ""
)
