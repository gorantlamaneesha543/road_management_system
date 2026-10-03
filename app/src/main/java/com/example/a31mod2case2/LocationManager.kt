package com.example.a31mod2case2

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class GpsLocation(
    val latitude: Double,
    val longitude: Double
)

class LocationManager(
    private val context: Context
) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GpsLocation? {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(
                            GpsLocation(
                                location.latitude,
                                location.longitude
                            )
                        )
                    } else {
                        val cancellationTokenSource = CancellationTokenSource()
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            cancellationTokenSource.token
                        ).addOnSuccessListener { freshLocation ->
                            if (freshLocation != null) {
                                continuation.resume(
                                    GpsLocation(
                                        freshLocation.latitude,
                                        freshLocation.longitude
                                    )
                                )
                            } else {
                                // Default fallback GPS coordinate if none available
                                continuation.resume(
                                    GpsLocation(
                                        37.4219983,
                                        -122.084
                                    )
                                )
                            }
                        }.addOnFailureListener {
                            continuation.resume(
                                GpsLocation(
                                    37.4219983,
                                    -122.084
                                )
                            )
                        }
                    }
                }
                .addOnFailureListener {
                    continuation.resume(
                        GpsLocation(
                            37.4219983,
                            -122.084
                        )
                    )
                }
        }
    }
}
