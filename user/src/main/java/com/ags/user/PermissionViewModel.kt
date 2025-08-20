package com.ags.user

import android.app.Application
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ags.core.model.ContactInfo
import com.ags.core.model.PermissionStatus
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
        val currentUser = firebaseAuth.currentUser ?: return
        val email = currentUser.email ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uploadState.value = UploadState.Loading // 👈 show loading

                val contacts = mutableListOf<ContactInfo>()

                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null, null, null
                )

                cursor?.use {
                    val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    while (it.moveToNext()) {
                        val name = it.getString(nameIdx)
                        val phone = it.getString(numberIdx)?.replace("\\D".toRegex(), "")
                        if (!phone.isNullOrEmpty()) {
                            contacts.add(ContactInfo(name, phone))
                        }
                    }
                }

                if (contacts.isEmpty()) {
                    _uploadState.value = UploadState.Error("No contacts found")
                    return@launch
                }

                // Deduplicate by phone number
                val uniqueContacts = contacts.distinctBy { it.phone }

                // Upload contacts in Firestore
                val userContactsRef = firestore.collection("users")
                    .document(email)
                    .collection("permissions")
                    .document("read_contacts")
                    .collection("contacts")

                // Firestore batch limit = 500 writes
                uniqueContacts.chunked(500).forEach { chunk ->
                    val batch = firestore.batch()
                    chunk.forEach { contact ->
                        val docRef = userContactsRef.document(contact.phone!!)
                        batch.set(docRef, contact)
                    }
                    batch.commit().await()
                }

                _uploadState.value = UploadState.Success("Contacts uploaded successfully ✅")

            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Failed: ${e.message}")
            }
        }
    }
}