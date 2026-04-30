package com.koflox.strava.impl.presentation.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.designsystem.component.DebouncedButton
import com.koflox.designsystem.component.DebouncedOutlinedButton
import com.koflox.designsystem.theme.Spacing
import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.api.model.SyncErrorReason
import com.koflox.strava.impl.R
import com.koflox.strava.impl.oauth.StravaAuthIntentLauncher
import com.koflox.strava.impl.presentation.components.StravaConnectButton
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private const val POWERED_BY_WIDTH_FRACTION = 0.2f

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface StravaShareTabEntryPoint {
    fun stravaAuthIntentLauncher(): StravaAuthIntentLauncher
}

@Composable
internal fun StravaShareTabRoute(
    sessionId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context, StravaShareTabEntryPoint::class.java)
    }
    val authIntentLauncher = entryPoint.stravaAuthIntentLauncher()
    val viewModel: StravaShareViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(sessionId) {
        viewModel.onEvent(StravaShareUiEvent.Started(sessionId))
    }
    LaunchedEffect(viewModel) {
        viewModel.navigation.collect { nav ->
            when (nav) {
                StravaShareNavigation.LaunchOAuthIntent -> authIntentLauncher.launch(context)
                is StravaShareNavigation.OpenStravaActivity ->
                    StravaActivityIntent.open(context, nav.activityId)
            }
        }
    }
    StravaShareTabContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
internal fun StravaShareTabContent(
    uiState: StravaShareUiState,
    onEvent: (StravaShareUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        when (uiState) {
            StravaShareUiState.Loading -> CircularProgressIndicator()
            is StravaShareUiState.Content -> StravaShareBody(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun StravaShareBody(
    uiState: StravaShareUiState.Content,
    onEvent: (StravaShareUiEvent) -> Unit,
) {
    when (uiState.authState) {
        StravaAuthState.LoggedOut -> StravaLoggedOutBody(onEvent = onEvent)
        is StravaAuthState.LoggedIn -> {
            StravaLoggedInBody(
                syncStatus = uiState.syncStatus,
                refreshCooldownSeconds = uiState.refreshCooldownSeconds,
                onEvent = onEvent,
            )
            Spacer(modifier = Modifier.height(Spacing.Medium))
            PoweredByStrava()
        }
    }
}

@Composable
private fun StravaLoggedOutBody(onEvent: (StravaShareUiEvent) -> Unit) {
    Text(
        text = stringResource(R.string.strava_share_intro),
        style = MaterialTheme.typography.bodyMedium,
    )
    StravaConnectButton(onClick = { onEvent(StravaShareUiEvent.ConnectClicked) })
}

@Composable
private fun StravaLoggedInBody(
    syncStatus: SessionSyncStatus,
    refreshCooldownSeconds: Int,
    onEvent: (StravaShareUiEvent) -> Unit,
) {
    when (syncStatus) {
        SessionSyncStatus.NotSynced -> {
            Text(text = stringResource(R.string.strava_share_intro), style = MaterialTheme.typography.bodyMedium)
            DebouncedButton(onClick = { onEvent(StravaShareUiEvent.SyncClicked) }) {
                Text(text = stringResource(R.string.strava_share_sync_button))
            }
        }
        SessionSyncStatus.Pending -> InProgressRow(message = stringResource(R.string.strava_share_status_pending))
        SessionSyncStatus.Uploading -> InProgressRow(message = stringResource(R.string.strava_share_status_uploading))
        SessionSyncStatus.Processing -> {
            InProgressRow(message = stringResource(R.string.strava_share_status_processing))
            RefreshButton(cooldownSeconds = refreshCooldownSeconds, onEvent = onEvent)
        }
        is SessionSyncStatus.Synced -> {
            Text(
                text = stringResource(R.string.strava_share_status_synced),
                style = MaterialTheme.typography.bodyMedium,
            )
            ViewOnStravaButton(onClick = { onEvent(StravaShareUiEvent.ViewOnStravaClicked) })
        }
        is SessionSyncStatus.Error -> ErrorBody(error = syncStatus, onEvent = onEvent)
    }
}

@Composable
private fun RefreshButton(
    cooldownSeconds: Int,
    onEvent: (StravaShareUiEvent) -> Unit,
) {
    val isCooldown = cooldownSeconds > 0
    OutlinedButton(
        onClick = { onEvent(StravaShareUiEvent.RefreshClicked) },
        enabled = !isCooldown,
    ) {
        val label = if (isCooldown) {
            stringResource(R.string.strava_share_refresh_button_cooldown, cooldownSeconds)
        } else {
            stringResource(R.string.strava_share_refresh_button)
        }
        Text(text = label)
    }
}

@Composable
private fun InProgressRow(message: String) {
    CircularProgressIndicator(modifier = Modifier.size(32.dp))
    Text(text = message, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun ViewOnStravaButton(onClick: () -> Unit) {
    DebouncedButton(onClick = onClick) {
        Text(
            text = stringResource(R.string.strava_share_view_on_strava),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ErrorBody(
    error: SessionSyncStatus.Error,
    onEvent: (StravaShareUiEvent) -> Unit,
) {
    Text(
        text = stringResource(error.reason.messageRes()),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
    val isAuthError = error.reason == SyncErrorReason.AUTH_REQUIRED
    if (isAuthError) {
        StravaConnectButton(onClick = { onEvent(StravaShareUiEvent.ConnectClicked) })
    } else if (error.isRetryable) {
        DebouncedOutlinedButton(onClick = { onEvent(StravaShareUiEvent.RetryClicked) }) {
            Text(text = stringResource(R.string.strava_share_retry_button))
        }
    }
}

@Composable
private fun PoweredByStrava() {
    Image(
        painter = painterResource(R.drawable.ic_strava_powered_by),
        contentDescription = stringResource(R.string.strava_share_powered_by),
        modifier = Modifier.fillMaxWidth(POWERED_BY_WIDTH_FRACTION),
    )
}

@Suppress("CyclomaticComplexMethod")
private fun SyncErrorReason.messageRes(): Int = when (this) {
    SyncErrorReason.NETWORK -> R.string.strava_share_error_network
    SyncErrorReason.AUTH_REQUIRED -> R.string.strava_share_error_auth
    SyncErrorReason.RATE_LIMITED -> R.string.strava_share_error_rate_limited
    SyncErrorReason.INVALID_ACTIVITY -> R.string.strava_share_error_invalid
    SyncErrorReason.POLL_TIMEOUT -> R.string.strava_share_error_poll_timeout
    SyncErrorReason.SERVER -> R.string.strava_share_error_server
    SyncErrorReason.UNKNOWN -> R.string.strava_share_error_unknown
}
