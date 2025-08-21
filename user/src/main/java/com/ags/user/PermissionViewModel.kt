package com.ags.user

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ags.core.model.PermissionStatus
import com.ags.user.features.readContacts.ContactsUploader
import com.ags.user.features.fineLocation.LocationUploader
import com.ags.user.features.fineLocation.LocationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PermissionViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val appContext = application.applicationContext

    private val contactsUploader = ContactsUploader(firestore)
    private val locationUploader = LocationUploader(firestore, appContext)

    // State exposed to UI
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> get() = _uploadState

    private val _locationUploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val locationUploadState: StateFlow<UploadState> get() = _locationUploadState

    private val email = firebaseAuth.currentUser?.email

    fun checkAndUploadPermission(permissions: List<String>, activity: Activity) {
        permissions.forEach { permission ->
            val granted = ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED
            uploadPermission(permission, granted)

            when {
                granted && permission == Manifest.permission.READ_CONTACTS -> uploadContacts()
                granted && permission == Manifest.permission.ACCESS_FINE_LOCATION -> uploadLocation(activity)
            }
        }
    }

    fun uploadPermission(permission: String, granted: Boolean) {
        val email = firebaseAuth.currentUser?.email ?: return
        val key = permissionToKey(permission)

        val status = PermissionStatus(
            permissionName = permission,
            granted = granted,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val docRef = firestore.collection("users")
                    .document(email)
                    .collection("permissions")
                    .document(key)

                val existing = docRef.get().await().toObject(PermissionStatus::class.java)
                if (existing?.granted != granted) { // only update if changed
                    docRef.set(status)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Convert full permission string to readable key
    private fun permissionToKey(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_CONTACTS -> "read_contacts"
            Manifest.permission.CAMERA -> "camera"
            Manifest.permission.RECORD_AUDIO -> "record_audio"
            Manifest.permission.ACCESS_FINE_LOCATION -> "fine_location"
            else -> permission.replace(".", "_")
        }
    }


    fun uploadContacts() {
        val email = firebaseAuth.currentUser?.email ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _uploadState.value = UploadState.Loading // 👈 show loading
            try {
                val message = contactsUploader.uploadContacts(appContext, email)
                _uploadState.value = UploadState.Success(message)
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Failed: ${e.message}")
            }
        }
    }

    fun uploadLocation(activity: Activity) {
        if (email == null) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Check GPS settings first
                val resolvable = LocationUtils.checkLocationSettings(activity)
                if (resolvable != null) {
                    // Reset to Idle while user is deciding (so spinner hides)
                    _locationUploadState.value = UploadState.Idle

                    withContext(Dispatchers.Main) {
                        val intentSenderRequest = IntentSenderRequest.Builder(resolvable.resolution).build()
                        if (activity is UserDashboardActivity) {
                            activity.resolutionLauncher.launch(intentSenderRequest)
                        }
                    }
                    return@launch
                }

                _locationUploadState.value = UploadState.Loading
                // If GPS is ON → upload location
                val message = locationUploader.uploadLocation(email)
                _locationUploadState.value = UploadState.Success(message)
            } catch (e: Exception) {
                _locationUploadState.value = UploadState.Error("Failed: ${e.message}")
            }
        }
    }

    fun onLocationUploadError() {
        _locationUploadState.value = UploadState.Error(
            "We couldn’t update your location because GPS is off."
        )
    }
}