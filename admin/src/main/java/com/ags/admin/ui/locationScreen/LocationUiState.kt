package com.ags.admin.ui.locationScreen

import com.ags.core.model.LocationData

sealed class LocationUiState {
    object Loading : LocationUiState()
    data class Success(val locations: List<LocationData>) : LocationUiState()
    data class Error(val message: String) : LocationUiState()
}