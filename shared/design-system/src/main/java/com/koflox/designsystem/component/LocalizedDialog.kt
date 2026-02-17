package com.koflox.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LocalizedDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        CompositionLocalProvider(
            LocalContext provides context,
            LocalConfiguration provides configuration,
        ) {
            content()
        }
    }
}
