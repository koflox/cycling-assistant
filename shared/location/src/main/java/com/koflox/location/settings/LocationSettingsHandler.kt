package com.koflox.location.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject

@Composable
fun LocationSettingsHandler(
    onLocationEnabled: () -> Unit,
    onLocationDenied: () -> Unit,
) {
    val locationSettingsDataSource: LocationSettingsDataSource = koinInject()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onLocationEnabled()
        } else {
            onLocationDenied()
        }
    }
    LaunchedEffect(Unit) {
        when (val result = locationSettingsDataSource.resolveLocationSettings()) {
            LocationSettingsResult.Enabled -> onLocationEnabled()
            is LocationSettingsResult.ResolutionRequired -> {
                launcher.launch(
                    IntentSenderRequest.Builder(result.intentSender).build(),
                )
            }

            LocationSettingsResult.Unavailable -> onLocationDenied()
        }
    }
}
