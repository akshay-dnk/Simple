package com.ags.admin.ui.smsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ags.core.model.SMSInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SMSViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _smsUiState = MutableStateFlow<SMSUiState>(SMSUiState.Loading)
    val smsUiState: StateFlow<SMSUiState> = _smsUiState

    fun loadUserSMS(email: String) {
        viewModelScope.launch {
            _smsUiState.value = SMSUiState.Loading
            try {
                val smsSnapshot = firestore.collection("users")
                    .document(email)
                    .collection("permissions")
                    .document("read_sms")
                    .collection("sms")
                    .get()
                    .await()

                val smsList = smsSnapshot.toObjects(SMSInfo::class.java)
                _smsUiState.value = SMSUiState.Success(smsList)
            } catch (e: Exception) {
                e.printStackTrace()
                _smsUiState.value = SMSUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }
}