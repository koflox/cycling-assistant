package com.koflox.destinations.presentation.permission

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun LocationPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: (isRationaleAvailable: Boolean) -> Unit,
    retryTrigger: Int,
    content: @Composable () -> Unit,
) {
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    var denialCount by remember { mutableIntStateOf(0) }
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION,
    ) { isGranted ->
        if (!isGranted) {
            denialCount++
        }
    }
    LaunchedEffect(permissionState.status) {
        when {
            permissionState.status.isGranted -> onPermissionGranted()
            !hasRequestedPermission -> {
                @Suppress("AssignedValueIsNeverRead")
                hasRequestedPermission = true
                permissionState.launchPermissionRequest()
            }
            else -> onPermissionDenied(permissionState.status.shouldShowRationale)
        }
    }
    LaunchedEffect(denialCount) {
        if (denialCount > 0) {
            onPermissionDenied(permissionState.status.shouldShowRationale)
        }
    }
    LaunchedEffect(retryTrigger) {
        if (retryTrigger > 0) {
            permissionState.launchPermissionRequest()
        }
    }
    content()
}
