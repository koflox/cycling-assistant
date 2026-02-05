package com.koflox.location.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class LocationSettingsDataSourceImpl(
    private val context: Context,
) : LocationSettingsDataSource {

    companion object {
        private const val LOCATION_CHECK_INTERVAL_MS = 3000L
    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun isLocationEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    override fun observeLocationEnabled(): Flow<Boolean> = callbackFlow {
        trySend(isLocationEnabled())
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(isLocationEnabled())
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION),
        )
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    override suspend fun resolveLocationSettings(): LocationSettingsResult {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_CHECK_INTERVAL_MS,
        ).build()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
        val settingsClient = LocationServices.getSettingsClient(context)
        return suspendCoroutine { continuation ->
            settingsClient.checkLocationSettings(settingsRequest)
                .addOnSuccessListener {
                    continuation.resume(LocationSettingsResult.Enabled)
                }
                .addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        continuation.resume(
                            LocationSettingsResult.ResolutionRequired(exception.resolution.intentSender),
                        )
                    } else {
                        continuation.resume(LocationSettingsResult.Unavailable)
                    }
                }
        }
    }
}
