package com.ags.admin.ui.appUsageScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ags.admin.ui.appUsageScreen.dateScreenFragment.AppUsageDateUiState
import com.ags.admin.ui.appUsageScreen.detailScreenFragment.AppUsageDetailUiState
import com.ags.core.model.AppUsageDateInfo
import com.ags.core.model.AppUsageInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AppUsageViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _appUsageDateUiState = MutableStateFlow<AppUsageDateUiState>(AppUsageDateUiState.Loading)
    val appUsageDateUiState: StateFlow<AppUsageDateUiState> = _appUsageDateUiState

    private val _appUsageDetailUiState = MutableStateFlow<AppUsageDetailUiState>(AppUsageDetailUiState.Loading)
    val appUsageDetailUiState: StateFlow<AppUsageDetailUiState> = _appUsageDetailUiState


    fun loadAvailableDates(email: String) {
        viewModelScope.launch {
            _appUsageDateUiState.value = AppUsageDateUiState.Loading
            try {
                val dateSnapshot = firestore.collection("users")
                    .document(email)
                    .collection("permissions")
                    .document("app_usage")
                    .collection("dates")
                    .get()
                    .await()

//                val dates = dateSnapshot.documents
//                    .map { document ->
//                        val date = document.getString("date") ?: ""
//                        val timestamp = document.getLong("timestamp") ?: 0L
//                        val totalApps = document.getLong("totalApps")?.toInt() ?: 0
//                        val totalUsageTime = document.getLong("totalUsageTime") ?: 0L
//                        AppUsageDateInfo(date, timestamp, totalApps, totalUsageTime)
//                    }
                val dateList = dateSnapshot.toObjects(AppUsageDateInfo::class.java)
                    .sortedByDescending { it.timestamp } // sort dates by timestamp

                _appUsageDateUiState.value = AppUsageDateUiState.Success(dateList)

            } catch (e: Exception) {
                e.printStackTrace()
                _appUsageDateUiState.value = AppUsageDateUiState.Error(e.message ?: "Something went wrong")
            }
        }

    }

    fun loadAppUsageDetail(email: String, date: String) {
        viewModelScope.launch {
            _appUsageDetailUiState.value = AppUsageDetailUiState.Loading
            try {
                val appUsageSnapshot = firestore.collection("users")
                    .document(email)
                    .collection("permissions")
                    .document("app_usage")
                    .collection("dates")
                    .document(date)
                    .collection("apps")
                    .get()
                    .await()

                val appUsageList = appUsageSnapshot.toObjects(AppUsageInfo::class.java)
                    .sortedByDescending { it.totalTimeVisible } // sort apps by usage

                _appUsageDetailUiState.value = AppUsageDetailUiState.Success(appUsageList)

            } catch (e: Exception) {
                e.printStackTrace()
                _appUsageDetailUiState.value = AppUsageDetailUiState.Error(e.message ?: "Something went wrong")
            }

        }
    }
}