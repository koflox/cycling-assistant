package com.koflox.connections.presentation.listing.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.koflox.connections.R
import com.koflox.designsystem.component.LocalizedAlertDialog

@Composable
internal fun DeleteConfirmationDialog(
    deviceName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    LocalizedAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.connections_delete_title)) },
        text = { Text(text = stringResource(R.string.connections_delete_message, deviceName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.connections_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.connections_delete_cancel))
            }
        },
    )
}
