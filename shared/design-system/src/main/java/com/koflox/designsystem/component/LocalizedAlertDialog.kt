package com.koflox.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties

/**
 * A locale-aware wrapper around Material 3 [AlertDialog].
 *
 * [AlertDialog] creates a new window whose composition does not inherit
 * [LocalContext] or [LocalConfiguration] from the parent. This means
 * `stringResource()` inside the dialog resolves strings using the system
 * locale instead of the app-selected locale.
 *
 * This composable captures both locals before the dialog opens and
 * re-provides them inside every composable lambda ([title], [text],
 * [confirmButton], [dismissButton], [icon]), so that all string
 * resolution inside the dialog respects the app locale.
 */
@Composable
fun LocalizedAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = { Localized(context, configuration) { confirmButton() } },
        modifier = modifier,
        dismissButton = dismissButton?.let { content -> { Localized(context, configuration) { content() } } },
        icon = icon?.let { content -> { Localized(context, configuration) { content() } } },
        title = title?.let { content -> { Localized(context, configuration) { content() } } },
        text = text?.let { content -> { Localized(context, configuration) { content() } } },
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
    )
}

@Composable
private fun Localized(
    context: android.content.Context,
    configuration: android.content.res.Configuration,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalContext provides context,
        LocalConfiguration provides configuration,
    ) {
        content()
    }
}
