package com.koflox.strava.impl.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.impl.R

private const val STRAVA_BADGE_WIDTH_FRACTION = 0.2f

@Composable
internal fun StravaSettingsSectionRoute(
    onNavigateToConnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: StravaSettingsViewModel = hiltViewModel()
    val authState by viewModel.authState.collectAsState()
    StravaSettingsSectionContent(
        authState = authState,
        onNavigateToConnect = onNavigateToConnect,
        modifier = modifier,
    )
}

@Composable
private fun StravaSettingsSectionContent(
    authState: StravaAuthState,
    onNavigateToConnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToConnect),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Image(
                painter = painterResource(R.drawable.ic_strava_compatible),
                contentDescription = stringResource(R.string.strava_compatible_content_description),
                modifier = Modifier.fillMaxWidth(STRAVA_BADGE_WIDTH_FRACTION),
            )
            val secondaryText = when (authState) {
                StravaAuthState.LoggedOut -> stringResource(R.string.strava_settings_connect_action)
                is StravaAuthState.LoggedIn -> stringResource(R.string.strava_connected_as, authState.athleteName)
            }
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
