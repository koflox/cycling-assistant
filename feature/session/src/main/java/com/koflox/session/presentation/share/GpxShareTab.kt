package com.koflox.session.presentation.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.koflox.designsystem.component.DebouncedButton
import com.koflox.designsystem.text.resolve
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R

@Composable
internal fun GpxShareTab(
    gpxShareState: GpxShareState,
    onEvent: (ShareUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        when (gpxShareState) {
            GpxShareState.Idle -> GpxIdleContent(onExportClick = { onEvent(ShareUiEvent.Gpx.ShareClicked) })
            GpxShareState.Generating -> GpxGeneratingContent()
            is GpxShareState.Ready -> GpxExportableContent(onExportClick = { onEvent(ShareUiEvent.Gpx.ShareClicked) })
            is GpxShareState.Error -> GpxErrorContent(message = gpxShareState.message)
            GpxShareState.Unavailable -> GpxUnavailableContent()
        }
    }
}

@Composable
private fun GpxIdleContent(onExportClick: () -> Unit) {
    GpxFileIcon(tint = MaterialTheme.colorScheme.primary)
    Text(
        text = stringResource(R.string.gpx_export_description),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    GpxExportButton(onClick = onExportClick, isEnabled = true)
}

@Composable
private fun GpxGeneratingContent() {
    CircularProgressIndicator()
    Text(
        text = stringResource(R.string.gpx_export_generating),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    GpxExportButton(onClick = {}, isEnabled = false)
}

@Composable
private fun GpxExportableContent(onExportClick: () -> Unit) {
    GpxFileIcon(tint = MaterialTheme.colorScheme.primary)
    GpxExportButton(onClick = onExportClick, isEnabled = true)
}

@Composable
private fun GpxErrorContent(message: com.koflox.designsystem.text.UiText) {
    val context = LocalContext.current
    Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.error,
    )
    Text(
        text = message.resolve(context),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.error,
    )
    GpxExportButton(onClick = {}, isEnabled = false)
}

@Composable
private fun GpxUnavailableContent() {
    Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.error,
    )
    Text(
        text = stringResource(R.string.gpx_export_no_app),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    GpxExportButton(onClick = {}, isEnabled = false)
}

@Composable
private fun GpxFileIcon(tint: Color) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = tint,
    )
}

@Composable
private fun GpxExportButton(onClick: () -> Unit, isEnabled: Boolean) {
    DebouncedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.gpx_export_button))
    }
}
