package com.ags.admin.ui.smsScreen

import com.ags.core.model.SMSInfo

sealed class SMSUiState {
    object Loading : SMSUiState()
    data class Success(val smsList: List<SMSInfo>) : SMSUiState()
    data class Error(val message: String) : SMSUiState()
}