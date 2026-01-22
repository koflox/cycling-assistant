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
import androidx.compose.ui.unit.dp
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
    LaunchedEffect(uiState.shareIntent) {
        uiState.shareIntent?.let { intent ->
            context.startActivity(intent)
            viewModel.onEvent(SessionsListUiEvent.ShareIntentLaunched)
        }
    }
    LaunchedEffect(uiState.shareError) {
        uiState.shareError?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(SessionsListUiEvent.ShareErrorDismissed)
        }
    }
    uiState.sharePreviewData?.let { data ->
        SharePreviewDialog(
            data = data,
            isSharing = uiState.isSharing,
            onShareClick = { bitmap, destinationName ->
                viewModel.onEvent(SessionsListUiEvent.ShareConfirmed(bitmap, destinationName))
            },
            onDismiss = { viewModel.onEvent(SessionsListUiEvent.ShareDialogDismissed) },
        )
    }
    SessionsListContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSessionClick = onSessionClick,
        onShareClick = { viewModel.onEvent(SessionsListUiEvent.ShareClicked(it)) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionsListContent(
    uiState: SessionsListUiState,
    onBackClick: () -> Unit,
    onSessionClick: (sessionId: String) -> Unit,
    onShareClick: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
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
        when {
            uiState.isLoading -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            uiState.isEmpty -> {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
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
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.sessions_list_empty_hint),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(32.dp),
        )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = session.destinationName,
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
            Spacer(modifier = Modifier.height(8.dp))
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
                    text = session.distanceFormatted,
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
