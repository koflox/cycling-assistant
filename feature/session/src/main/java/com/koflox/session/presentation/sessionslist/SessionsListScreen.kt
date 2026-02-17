package com.koflox.session.presentation.sessionslist

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.koflox.designsystem.text.resolve
import com.koflox.designsystem.theme.Elevation
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R
import com.koflox.session.presentation.share.SharePreviewDialog
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SessionsListRoute(
    onBackClick: () -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SessionsListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(uiState) {
        val content = uiState as? SessionsListUiState.Content ?: return@LaunchedEffect
        val overlay = content.overlay
        if (overlay is SessionsListOverlay.ShareReady) {
            context.startActivity(overlay.intent)
            viewModel.onEvent(SessionsListUiEvent.ShareIntentLaunched)
            return@LaunchedEffect
        }
        val (message, dismissEvent) = when (overlay) {
            is SessionsListOverlay.ShareError -> overlay.message.resolve(context) to SessionsListUiEvent.ShareErrorDismissed
            is SessionsListOverlay.LoadError -> overlay.message.resolve(context) to SessionsListUiEvent.LoadErrorDismissed
            else -> return@LaunchedEffect
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.onEvent(dismissEvent)
    }
    SessionsListContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSessionClick = onSessionClick,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionsListContent(
    uiState: SessionsListUiState,
    onBackClick: () -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
    onEvent: (SessionsListUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = uiState as? SessionsListUiState.Content
    val overlay = content?.overlay
    if (overlay != null && overlay !is SessionsListOverlay.ShareReady) {
        val previewData = when (overlay) {
            is SessionsListOverlay.SharePreview -> overlay.data
            is SessionsListOverlay.Sharing -> overlay.data
            is SessionsListOverlay.ShareError -> overlay.data
            is SessionsListOverlay.ShareReady -> null
            is SessionsListOverlay.LoadError -> null
        }
        previewData?.let { data ->
            SharePreviewDialog(
                data = data,
                isSharing = overlay is SessionsListOverlay.Sharing,
                onShareClick = { bitmap, shareText, chooserTitle ->
                    onEvent(SessionsListUiEvent.ShareConfirmed(bitmap, shareText, chooserTitle))
                },
                onDismiss = { onEvent(SessionsListUiEvent.ShareDialogDismissed) },
            )
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sessions_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sessions_list_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        SessionsListBody(
            uiState = uiState,
            onSessionClick = onSessionClick,
            onShareClick = { onEvent(SessionsListUiEvent.ShareClicked(it)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

@Composable
private fun SessionsListBody(
    uiState: SessionsListUiState,
    onSessionClick: (sessionId: String) -> Unit,
    onShareClick: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        SessionsListUiState.Loading -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        SessionsListUiState.Empty -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.sessions_list_empty_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Spacing.Huge),
                )
            }
        }

        is SessionsListUiState.Content -> {
            LazyColumn(
                modifier = modifier.padding(horizontal = Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
            ) {
                item { Spacer(modifier = Modifier.height(Spacing.Tiny)) }
                items(
                    items = uiState.sessions,
                    key = { it.id },
                ) { session ->
                    SessionListItem(
                        session = session,
                        onClick = { onSessionClick(session.id) },
                        onShareClick = { onShareClick(session.id) },
                    )
                }
                item { Spacer(modifier = Modifier.height(Spacing.Tiny)) }
            }
        }
    }
}

@Composable
private fun SessionListItem(
    session: SessionListItemUiModel,
    onClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Subtle),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Large),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = session.destinationName ?: stringResource(R.string.session_free_roam_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (session.isShareButtonVisible) {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_content_description),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                StatusChip(status = session.status)
            }
            Spacer(modifier = Modifier.height(Spacing.Small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = session.dateFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.session_stat_value_km, session.distanceFormatted),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: SessionListItemStatus,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = when (status) {
        SessionListItemStatus.RUNNING -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        SessionListItemStatus.PAUSED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        SessionListItemStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    val statusText = when (status) {
        SessionListItemStatus.RUNNING -> stringResource(R.string.sessions_list_status_running)
        SessionListItemStatus.PAUSED -> stringResource(R.string.sessions_list_status_paused)
        SessionListItemStatus.COMPLETED -> stringResource(R.string.sessions_list_status_completed)
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = Spacing.Small, vertical = Spacing.Tiny),
        )
    }
}
