package com.koflox.connections.presentation.permission

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.koflox.ble.permission.BlePermissionChecker
import com.koflox.connections.R
import com.koflox.designsystem.component.DebouncedButton
import com.koflox.designsystem.component.DebouncedOutlinedButton
import com.koflox.designsystem.theme.Spacing
import org.koin.compose.koinInject

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun BlePermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    blePermissionChecker: BlePermissionChecker = koinInject(),
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = blePermissionChecker.requiredPermissions(),
        onPermissionsResult = { results ->
            if (results.values.all { it }) {
                onPermissionGranted()
            }
        },
    )
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            onPermissionGranted()
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.connections_permission_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = stringResource(R.string.connections_permission_rationale),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        DebouncedButton(
            onClick = { permissionsState.launchMultiplePermissionRequest() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.connections_permission_grant))
        }
        Spacer(modifier = Modifier.height(Spacing.Small))
        DebouncedOutlinedButton(
            onClick = onPermissionDenied,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.connections_permission_skip))
        }
    }
}
