package com.koflox.session.presentation.permission

import android.Manifest
import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.koflox.session.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionHandler(
    onPermissionResult: (isGranted: Boolean) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        var showRationaleDialog by remember { mutableStateOf(false) }
        var hasAttemptedRequest by remember { mutableStateOf(false) }
        val permissionState = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
        ) { _ ->
            hasAttemptedRequest = true
            onPermissionResult(true) // Proceed with session regardless of permission result
        }
        LaunchedEffect(Unit) {
            when {
                permissionState.status.isGranted -> {
                    onPermissionResult(true)
                }

                permissionState.status.shouldShowRationale -> {
                    showRationaleDialog = true
                }

                !hasAttemptedRequest -> {
                    permissionState.launchPermissionRequest()
                }

                else -> { // Permission permanently denied, proceed without notification
                    onPermissionResult(true)
                }
            }
        }
        if (showRationaleDialog) {
            PermissionRationaleDialog(
                onGrantClick = {
                    @Suppress("AssignedValueIsNeverRead")
                    showRationaleDialog = false
                    permissionState.launchPermissionRequest()
                },
                onSkipClick = {
                    @Suppress("AssignedValueIsNeverRead")
                    showRationaleDialog = false
                    onPermissionResult(true)
                },
            )
        }
    } else {
        LaunchedEffect(Unit) {
            onPermissionResult(true)
        }
    }
}

@Composable
private fun PermissionRationaleDialog(
    onGrantClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onSkipClick,
        title = { Text(text = stringResource(R.string.permission_notification_title)) },
        text = { Text(text = stringResource(R.string.permission_notification_rationale)) },
        confirmButton = {
            TextButton(onClick = onGrantClick) {
                Text(text = stringResource(R.string.permission_button_grant))
            }
        },
        dismissButton = {
            TextButton(onClick = onSkipClick) {
                Text(text = stringResource(R.string.permission_button_skip))
            }
        },
    )
}
