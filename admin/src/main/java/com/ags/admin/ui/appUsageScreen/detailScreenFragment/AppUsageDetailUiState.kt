package com.ags.admin.ui.appUsageScreen.detailScreenFragment

import com.ags.core.model.AppUsageInfo

sealed class AppUsageDetailUiState {
    object Loading : AppUsageDetailUiState()
    data class Success(val appUsageList: List<AppUsageInfo>) : AppUsageDetailUiState()
    data class Error(val message: String) : AppUsageDetailUiState()
}