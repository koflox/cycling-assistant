package com.koflox.session.presentation.dialog

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.koflox.designsystem.component.LocalizedAlertDialog
import com.koflox.session.R

@Composable
fun StopConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    LocalizedAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.stop_confirmation_title))
        },
        text = {
            Text(text = stringResource(R.string.stop_confirmation_message))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(text = stringResource(R.string.stop_confirmation_stop))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.stop_confirmation_cancel))
            }
        },
    )
}
