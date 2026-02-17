package com.koflox.session.presentation.dialog

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.koflox.designsystem.component.LocalizedAlertDialog
import com.koflox.session.R

@Composable
fun LocationDisabledDialog(
    onEnableClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    LocalizedAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.location_disabled_title))
        },
        text = {
            Text(text = stringResource(R.string.location_disabled_message))
        },
        confirmButton = {
            TextButton(onClick = onEnableClick) {
                Text(text = stringResource(R.string.location_disabled_enable))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.location_disabled_dismiss))
            }
        },
    )
}
