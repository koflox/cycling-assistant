package com.koflox.session.presentation.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.koflox.session.R
import java.util.Locale

// TODO: fix overlapping of start session and cancel buttons
@Composable
fun DestinationConfirmationDialog(
    destinationName: String,
    distanceKm: Double,
    onNavigateClick: () -> Unit,
    onStartSessionClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = destinationName)
        },
        text = {
            Text(
                text = String.format(
                    Locale.getDefault(),
                    stringResource(R.string.dialog_distance_format),
                    distanceKm,
                ),
            )
        },
        confirmButton = {
            Column {
                Button(
                    onClick = onNavigateClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.dialog_button_navigate))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onStartSessionClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.dialog_button_start_session))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        },
    )
}
