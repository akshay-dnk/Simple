package com.ags.admin.ui.locationScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ags.core.model.LocationData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _locationUiState = MutableStateFlow<LocationUiState>(LocationUiState.Loading)
    val locationUiState: StateFlow<LocationUiState> = _locationUiState


    fun loadUserLocations(email: String) {
        viewModelScope.launch {
            _locationUiState.value = LocationUiState.Loading
            try {
                val snapshot = firestore.collection("users")
                    .document(email)
                    .collection("permissions")
                    .document("fine_location")
                    .collection("location")
                    .get()
                    .await()

                val locationData = snapshot.toObjects(LocationData::class.java)
                _locationUiState.value = LocationUiState.Success(locationData)

            } catch (e: Exception) {
                e.printStackTrace()
                _locationUiState.value = LocationUiState.Error(e.message ?: "Something went wrong")
            }
        }

    }
}