package com.koflox.destinations.presentation.permission

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun LocationPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    content: @Composable () -> Unit,
) {
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
    )
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(permissionState.status) {
        when {
            permissionState.status.isGranted -> onPermissionGranted()
            !hasRequestedPermission -> {
                @Suppress("AssignedValueIsNeverRead")
                hasRequestedPermission = true
                permissionState.launchPermissionRequest()
            }

            else -> onPermissionDenied()
        }
    }
    content()
}
