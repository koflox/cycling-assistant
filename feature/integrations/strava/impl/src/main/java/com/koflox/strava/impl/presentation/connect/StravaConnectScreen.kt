package com.koflox.strava.impl.presentation.connect

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.designsystem.component.DebouncedOutlinedButton
import com.koflox.designsystem.component.LocalizedAlertDialog
import com.koflox.designsystem.theme.Spacing
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.impl.R
import com.koflox.strava.impl.oauth.StravaAuthHint
import com.koflox.strava.impl.oauth.StravaAuthIntentLauncher
import com.koflox.strava.impl.presentation.components.StravaConnectButton
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface StravaConnectEntryPoint {
    fun stravaAuthIntentLauncher(): StravaAuthIntentLauncher
}

@Composable
internal fun StravaConnectRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context, StravaConnectEntryPoint::class.java)
    }
    val authIntentLauncher = entryPoint.stravaAuthIntentLauncher()
    val viewModel: StravaConnectViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.navigation.collect { navigation ->
            when (navigation) {
                StravaConnectNavigation.LaunchOAuthIntent -> authIntentLauncher.launch(context)
            }
        }
    }
    StravaConnectContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StravaConnectContent(
    uiState: StravaConnectUiState,
    onEvent: (StravaConnectUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.strava_connect_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.strava_connect_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.Large),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                StravaConnectUiState.Loading -> CircularProgressIndicator()
                is StravaConnectUiState.Content -> StravaConnectBody(
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun StravaConnectBody(
    uiState: StravaConnectUiState.Content,
    onEvent: (StravaConnectUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Large),
    ) {
        if (uiState.hint == StravaAuthHint.MissingRequiredScopes) {
            MissingScopesBanner(onEvent = onEvent)
        }
        when (val authState = uiState.authState) {
            StravaAuthState.LoggedOut -> StravaLoggedOutContent(onEvent = onEvent)
            is StravaAuthState.LoggedIn -> StravaLoggedInContent(authState = authState, onEvent = onEvent)
        }
    }
    if (uiState.overlay is StravaConnectUiState.Content.Overlay.LogoutConfirm) {
        LogoutConfirmDialog(onEvent = onEvent)
    }
}

@Composable
private fun MissingScopesBanner(onEvent: (StravaConnectUiEvent) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small),
        ) {
            Text(
                text = stringResource(R.string.strava_error_missing_scopes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            TextButton(
                onClick = { onEvent(StravaConnectUiEvent.HintDismissed) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.strava_error_missing_scopes_dismiss))
            }
        }
    }
}

@Composable
private fun StravaLoggedOutContent(onEvent: (StravaConnectUiEvent) -> Unit) {
    Text(
        text = stringResource(R.string.strava_connect_intro),
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        text = stringResource(R.string.strava_switch_account_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(Spacing.Medium))
    StravaConnectButton(onClick = { onEvent(StravaConnectUiEvent.ConnectClicked) })
}

@Composable
private fun StravaLoggedInContent(
    authState: StravaAuthState.LoggedIn,
    onEvent: (StravaConnectUiEvent) -> Unit,
) {
    val context = LocalContext.current
    Text(
        text = stringResource(R.string.strava_connected_as, authState.athleteName),
        style = MaterialTheme.typography.titleMedium,
    )
    DebouncedOutlinedButton(onClick = { onEvent(StravaConnectUiEvent.LogoutClicked) }) {
        Text(text = stringResource(R.string.strava_logout_button))
    }
    Spacer(modifier = Modifier.height(Spacing.Large))
    PrivacyHint(onOpenSettings = { openStravaPrivacySettings(context) })
}

@Composable
private fun PrivacyHint(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.strava_privacy_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onOpenSettings) {
            Text(text = stringResource(R.string.strava_privacy_open_settings))
        }
    }
}

private const val STRAVA_PRIVACY_URL = "https://www.strava.com/settings/privacy"

private fun openStravaPrivacySettings(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(STRAVA_PRIVACY_URL))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

@Composable
private fun LogoutConfirmDialog(onEvent: (StravaConnectUiEvent) -> Unit) {
    LocalizedAlertDialog(
        onDismissRequest = { onEvent(StravaConnectUiEvent.LogoutDismissed) },
        title = { Text(stringResource(R.string.strava_logout_confirm_title)) },
        text = { Text(stringResource(R.string.strava_logout_confirm_message)) },
        confirmButton = {
            TextButton(onClick = { onEvent(StravaConnectUiEvent.LogoutConfirmed) }) {
                Text(stringResource(R.string.strava_logout_confirm_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(StravaConnectUiEvent.LogoutDismissed) }) {
                Text(stringResource(R.string.strava_logout_confirm_no))
            }
        },
    )
}
