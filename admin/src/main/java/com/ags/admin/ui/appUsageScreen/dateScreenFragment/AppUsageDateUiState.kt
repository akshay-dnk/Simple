package com.ags.admin.ui.appUsageScreen.dateScreenFragment

import com.ags.core.model.AppUsageDateInfo

sealed class AppUsageDateUiState {
    object Loading : AppUsageDateUiState()
    data class Success(val dateList: List<AppUsageDateInfo>) : AppUsageDateUiState()
    data class Error(val message: String) : AppUsageDateUiState()
}