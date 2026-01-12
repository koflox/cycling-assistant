package com.koflox.cyclingassistant.data.source.location

import android.annotation.SuppressLint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.koflox.cyclingassistant.domain.model.Location
import kotlinx.coroutines.CoroutineDispatcher
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
                            Result.success(
                                Location(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                ),
                            ),
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
}
