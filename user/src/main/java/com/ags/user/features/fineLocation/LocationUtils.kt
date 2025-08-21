package com.ags.user.features.fineLocation

import android.content.Context
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

object LocationUtils {

//    suspend fun isLocationEnabled(context: Context, activity: Activity): Boolean {
//        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
//
//        val builder = LocationSettingsRequest.Builder()
//            .addLocationRequest(locationRequest)
//            .setAlwaysShow(true) // ensures dialog shows
//
//        val settingsClient = LocationServices.getSettingsClient(context)
//
//        return try {
//            settingsClient.checkLocationSettings(builder.build()).await()
//            true // Location is ON
//        } catch (e: ResolvableApiException) {
//            try {
//                // Location is OFF → Ask user to turn it on
//                e.startResolutionForResult(activity, 1001)
//            } catch (_: IntentSender.SendIntentException) {}
//            false
//        } catch (e: Exception) {
//            false
//        }
//    }

    suspend fun checkLocationSettings(context: Context): ResolvableApiException? {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(context)

        return try {
            settingsClient.checkLocationSettings(builder.build()).await()
            null // Already enabled
        } catch (e: ResolvableApiException) {
            e // Disabled, return the exception so caller can resolve
        } catch (_: Exception) {
            null
        }
    }
}