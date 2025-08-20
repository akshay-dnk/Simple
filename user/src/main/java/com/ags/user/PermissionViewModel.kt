package com.ags.user

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ags.core.model.PermissionStatus
import com.ags.user.data.ContactsUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PermissionViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val contactsUploader = ContactsUploader(firestore)

    // State exposed to UI
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> get() = _uploadState

    fun checkAndUploadPermission(permissions: List<String>) {
        val context = getApplication<Application>().applicationContext
        permissions.forEach { permission ->
            val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            uploadPermission(permission, granted)

            if (granted && permission == android.Manifest.permission.READ_CONTACTS) {
                uploadContacts() // upload contacts if granted
            }
        }
    }

    fun uploadPermission(permission: String, granted: Boolean) {
        val currentUser = firebaseAuth.currentUser ?: return
        val email = currentUser.email ?: return
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
            android.Manifest.permission.READ_CONTACTS -> "read_contacts"
            android.Manifest.permission.CAMERA -> "camera"
            android.Manifest.permission.RECORD_AUDIO -> "record_audio"
            android.Manifest.permission.ACCESS_FINE_LOCATION -> "fine_location"
            else -> permission.replace(".", "_")
        }
    }


    fun uploadContacts() {
        val context = getApplication<Application>().applicationContext
        val email = firebaseAuth.currentUser?.email ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uploadState.value = UploadState.Loading // 👈 show loading
                val message = contactsUploader.uploadContacts(context, email)
                _uploadState.value = UploadState.Success(message)
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Failed: ${e.message}")
            }
        }
    }
}