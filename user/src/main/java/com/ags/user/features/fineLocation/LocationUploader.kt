package com.ags.user.features.fineLocation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.ags.core.model.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.coroutines.resume

class LocationUploader(
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun uploadLocation(userEmail: String): String {
        return try {
            val currentLocation = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            val lastLocation = fusedLocationClient.lastLocation.await() ?: return "Location not available"

            val location = currentLocation ?: lastLocation
            val geocoder = Geocoder(context, Locale.getDefault())
            val address = getAddressFromGeocoder(geocoder, location.latitude, location.longitude)

            val locationData = LocationData(
                latitude = location.latitude,
                longitude = location.longitude,
                address = address,
                timestamp = System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userEmail)
                .collection("permissions")
                .document("fine_location")
                .collection("location")
                .document("latest")
                .set(locationData)
                .await()

            "Location uploaded successfully ✅"
        } catch (e: Exception) {
            e.printStackTrace()
            "Failed to upload location: ${e.message}"
        }
    }

    private suspend fun getAddressFromGeocoder(
        geocoder: Geocoder,
        latitude: Double,
        longitude: Double
    ): String = suspendCancellableCoroutine { cont ->
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Modern async API for Android 13+
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    val addr = addresses.firstOrNull()?.getAddressLine(0) ?: "Address not available"
                    if (cont.isActive) cont.resume(addr)
                }
            } else {
                // Legacy blocking API for older devices
                val addr = try {
                    geocoder.getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()?.getAddressLine(0) ?: "Address not available"
                } catch (e: Exception) {
                    "Address not available"
                }
                if (cont.isActive) cont.resume(addr)
            }
        } catch (e: Exception) {
            if (cont.isActive) cont.resume("Address not available")
        }
    }
}