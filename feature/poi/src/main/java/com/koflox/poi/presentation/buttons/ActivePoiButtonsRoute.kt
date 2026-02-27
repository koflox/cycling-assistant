package com.koflox.poi.presentation.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.koflox.designsystem.component.DebouncedOutlinedButton
import com.koflox.designsystem.theme.Spacing
import com.koflox.poi.R
import com.koflox.poi.presentation.mapper.label
import org.koin.androidx.compose.koinViewModel

private val MoreButtonWidth = 40.dp

@Composable
fun ActivePoiButtonsRoute(
    onPoiClicked: (query: String) -> Unit,
    onNavigateToPoiSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivePoiButtonsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    when (val state = uiState) {
        is ActivePoiButtonsUiState.Loading -> Unit
        is ActivePoiButtonsUiState.Content -> {
            ActivePoiButtonsContent(
                state = state,
                onPoiClicked = onPoiClicked,
                onNavigateToPoiSelection = onNavigateToPoiSelection,
                onEvent = viewModel::onEvent,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ActivePoiButtonsContent(
    state: ActivePoiButtonsUiState.Content,
    onPoiClicked: (query: String) -> Unit,
    onNavigateToPoiSelection: () -> Unit,
    onEvent: (ActivePoiButtonsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val poiButtonColors = ButtonDefaults.outlinedButtonColors(
        containerColor = MaterialTheme.colorScheme.surface,
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.selectedPois.forEach { poiType ->
            val poiLabel = poiType.label()
            DebouncedOutlinedButton(
                onClick = { onPoiClicked(poiLabel) },
                modifier = Modifier.weight(1f),
                colors = poiButtonColors,
            ) {
                Text(
                    text = poiLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        DebouncedOutlinedButton(
            onClick = { onEvent(ActivePoiButtonsUiEvent.MoreClicked) },
            modifier = Modifier
                .width(MoreButtonWidth)
                .aspectRatio(1f),
            colors = poiButtonColors,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.poi_more_button_description),
            )
        }
    }
    if (state.isMoreDialogVisible) {
        MorePoisDialog(
            unselectedPois = state.unselectedPois,
            onPoiClicked = { query ->
                onPoiClicked(query)
                onEvent(ActivePoiButtonsUiEvent.MoreDialogDismissed)
            },
            onNavigateToPoiSelection = {
                onNavigateToPoiSelection()
                onEvent(ActivePoiButtonsUiEvent.MoreDialogDismissed)
            },
            onDismiss = { onEvent(ActivePoiButtonsUiEvent.MoreDialogDismissed) },
        )
    }
}
