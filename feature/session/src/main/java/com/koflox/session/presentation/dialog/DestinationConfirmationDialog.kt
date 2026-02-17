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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.koflox.designsystem.theme.Spacing
import com.koflox.location.model.Location
import com.koflox.location.settings.LocationSettingsHandler
import com.koflox.session.R
import com.koflox.session.domain.usecase.CreateSessionParams
import com.koflox.session.presentation.permission.NotificationPermissionHandler
import com.koflox.session.presentation.session.SessionViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

private enum class SessionStartStep {
    IDLE,
    CHECKING_LOCATION,
    STARTING_SESSION,
}

@Composable
fun DestinationOptionsRoute(
    destinationId: String,
    destinationName: String,
    destinationLocation: Location,
    distanceKm: Double,
    onSessionStarting: () -> Unit,
    onNavigateClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModel: SessionViewModel = koinViewModel()
    var startStep by remember { mutableStateOf(SessionStartStep.IDLE) }
    when (startStep) {
        SessionStartStep.IDLE -> Unit
        SessionStartStep.CHECKING_LOCATION -> LocationSettingsHandler(
            onLocationEnabled = {
                @Suppress("AssignedValueIsNeverRead")
                startStep = SessionStartStep.STARTING_SESSION
            },
            onLocationDenied = {
                @Suppress("AssignedValueIsNeverRead")
                startStep = SessionStartStep.IDLE
            },
        )
        SessionStartStep.STARTING_SESSION -> NotificationPermissionHandler { _ ->
            onSessionStarting()
            viewModel.startSession(
                CreateSessionParams.Destination(
                    destinationId = destinationId,
                    destinationName = destinationName,
                    destinationLatitude = destinationLocation.latitude,
                    destinationLongitude = destinationLocation.longitude,
                ),
            )
            onDismiss()
        }
    }
    DestinationConfirmationDialog(
        destinationName = destinationName,
        distanceKm = distanceKm,
        onNavigateClick = onNavigateClick,
        onStartSessionClick = {
            @Suppress("AssignedValueIsNeverRead")
            startStep = SessionStartStep.CHECKING_LOCATION
        },
        onDismiss = onDismiss,
    )
}

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
                Spacer(modifier = Modifier.height(Spacing.Small))
                OutlinedButton(
                    onClick = onStartSessionClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.dialog_button_start_session))
                }
                Spacer(modifier = Modifier.height(Spacing.Small))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.dialog_button_cancel))
                }
            }
        },
    )
}
