package com.koflox.location

import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.koflox.location.model.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class LocationDataSourceImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val fusedLocationClient: FusedLocationProviderClient,
) : LocationDataSource {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<Location> = withContext(dispatcherIo) {
        suspendCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token,
            )
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(
                            Result.success(mapLocation(location)),
                        )
                    } else {
                        continuation.resume(
                            Result.failure(Exception("Location unavailable")),
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    cancellationTokenSource.cancel()
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    @SuppressLint("MissingPermission")
    override fun observeLocationUpdates(
        intervalMs: Long,
        inUpdateDistanceMeters: Float,
    ): Flow<Location> = callbackFlow {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMs,
        )
            .setMinUpdateDistanceMeters(inUpdateDistanceMeters)
            .build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { androidLocation ->
                    trySend(
                        Location(
                            latitude = androidLocation.latitude,
                            longitude = androidLocation.longitude,
                        ),
                    )
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper(),
        )
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
