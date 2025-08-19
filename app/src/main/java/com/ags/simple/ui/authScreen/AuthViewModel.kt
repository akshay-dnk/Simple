package com.ags.simple.ui.authScreen

import android.app.Application
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ags.simple.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        // Check at startup
        if (isSignedIn()) {
            getOrCreateUserRole()
        } else {
            _authState.value = AuthState.FirstLaunch
        }
    }

    fun isSignedIn(): Boolean = firebaseAuth.currentUser != null

    suspend fun handleSignIn(result: GetCredentialResponse) {
        _authState.value = AuthState.Loading
        val credential = result.credential

        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                if (authResult.user != null) {
                    getOrCreateUserRole()
                } else {
                    _authState.value = AuthState.Error("Firebase sign-in failed")
                }
            } catch (e: GoogleIdTokenParsingException) {
                _authState.value = AuthState.Error("Google ID token parsing failed")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        } else {
            _authState.value = AuthState.Error("Invalid credential type")
        }
    }

    fun signIn(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(context.getString(R.string.default_web_client_id))
                            .setAutoSelectEnabled(false)
                            .setNonce(UUID.randomUUID().toString())
                            .build()
                    )
                    .build()

                val result = credentialManager.getCredential(context, request)
                handleSignIn(result)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        viewModelScope.launch {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            firebaseAuth.signOut()
            _authState.value = AuthState.SignedOut
        }
    }

    fun getOrCreateUserRole() {
        val currentUser = firebaseAuth.currentUser ?: return
        val email = currentUser.email ?: return

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").document(email).get().await()
                val role = if (snapshot.exists()) {
                    snapshot.getString("role") ?: "user"
                } else {
                    val newUser = hashMapOf(
                        "name" to (currentUser.displayName ?: ""),
                        "email" to email,
                        "role" to "user",
                        "profileUrl" to (currentUser.photoUrl?.toString() ?: "")
                    )
                    db.collection("users").document(email).set(newUser).await()
                    "user"
                }

                _authState.value = AuthState.SignedIn(
                    if (role == "admin") RoleState.Admin else RoleState.User
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to fetch role")
            }
        }
    }
}