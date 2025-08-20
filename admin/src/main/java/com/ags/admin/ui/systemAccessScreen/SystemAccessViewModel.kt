package com.ags.admin.ui.systemAccessScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ags.admin.R
import com.ags.admin.model.SystemAccess
import com.ags.admin.model.SystemFeatureType
import com.ags.core.model.PermissionStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SystemAccessViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _features = MutableStateFlow<List<SystemAccess>>(emptyList())
    val features: StateFlow<List<SystemAccess>> = _features

    private var listenerRegistration: ListenerRegistration? = null

    fun loadUserPermissions(email: String) {

        // Cancel old listener (avoid memory leaks if reloaded)
        listenerRegistration?.remove()

        listenerRegistration = firestore.collection("users")
            .document(email)
            .collection("permissions")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                viewModelScope.launch {
                    try {
                        val grantedKeys = snapshot.documents
                            .mapNotNull { it.toObject(PermissionStatus::class.java) }
                            .filter { it.granted }
                            .map { it.permissionName }

                        val allFeatures = buildAllFeatures()

                        _features.value = allFeatures.map { feature ->
                            feature.copy(enabled = grantedKeys.contains(feature.permissionName))
                        }

                    } catch (e: Exception) {
                        Log.e("SystemAccessViewModel", "Error mapping permissions", e)
                    }
                }

            }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up Firestore listener
        listenerRegistration?.remove()
    }

    private fun buildAllFeatures(): List<SystemAccess> = listOf(
        SystemAccess(
            type = SystemFeatureType.LIVE_CAMERA,
            title = "Live Camera",
            description = "Access device camera",
            iconRes = R.drawable.ic_camera,
            permissionName = android.Manifest.permission.CAMERA
        ),
        SystemAccess(
            type = SystemFeatureType.READ_CONTACTS,
            title = "Read Contacts",
            description = "Access phone contacts",
            iconRes = R.drawable.ic_contacts,
            permissionName = android.Manifest.permission.READ_CONTACTS
        ),
        SystemAccess(
            type = SystemFeatureType.FINE_LOCATION,
            title = "Fine Location",
            description = "Get GPS location",
            iconRes = R.drawable.ic_location,
            permissionName = android.Manifest.permission.ACCESS_FINE_LOCATION
        ),
        SystemAccess(
            type = SystemFeatureType.RECORD_AUDIO,
            title = "Record Audio",
            description = "Use microphone",
            iconRes = R.drawable.ic_mic,
            permissionName = android.Manifest.permission.RECORD_AUDIO
        )
    )
}