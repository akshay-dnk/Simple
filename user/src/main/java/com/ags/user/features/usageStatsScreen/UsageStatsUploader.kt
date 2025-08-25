package com.ags.user.features.usageStatsScreen

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import com.ags.core.model.AppUsageDateInfo
import com.ags.core.model.AppUsageInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsageStatsUploader(private val firestore: FirebaseFirestore) {

    suspend fun uploadUsageStats(
        context: Context,
        userEmail: String,
        durationMillis: Long = 60 * 60 * 1000L
    ): String {
        val usageStatsList = getRecentApps(context, durationMillis)

        if (usageStatsList.isEmpty()) return "No usage stats found"

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val uploadedAt = System.currentTimeMillis()

        // Convert to model
        val apps = usageStatsList
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .map {
                AppUsageInfo(
                    packageName = it.packageName,
                    appName = try {
                        context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(it.packageName, 0)).toString()
                    } catch (e: Exception) {
                        it.packageName
                    },
                    lastTimeUsed = it.lastTimeUsed,
                    totalTimeVisible = it.totalTimeInForeground,
                )
            }

        val usageRef = firestore.collection("users")
            .document(userEmail)
            .collection("permissions")
            .document("app_usage")
            .collection("dates")
            .document(date)

        usageRef.set(
            AppUsageDateInfo(
                date = date,
                timestamp = uploadedAt,
                totalApps = apps.size,
                totalUsageTime = apps.sumOf { it.totalTimeVisible }
            )
        )

        // Batch write (500 limit safeguard)
        apps.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { app ->
                val docRef = usageRef.collection("apps").document(app.packageName)
                batch.set(docRef, app)
            }
            batch.commit().await()
        }

        return "App usage uploaded successfully ✅"
    }


    private fun getRecentApps(context: Context, durationMillis: Long): List<UsageStats> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - durationMillis
        return usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime) ?: emptyList()
    }
}