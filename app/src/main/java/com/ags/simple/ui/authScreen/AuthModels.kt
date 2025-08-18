package com.ags.simple.ui.authScreen

// Authentication states
sealed class AuthState {
    object FirstLaunch : AuthState()
    object Idle : AuthState()
    object Loading : AuthState()
    data class SignedIn(val role: RoleState) : AuthState()
    object SignedOut : AuthState()
    data class Error(val message: String) : AuthState()
}

// User roles
sealed class RoleState {
    object Admin : RoleState()
    object User : RoleState()
}