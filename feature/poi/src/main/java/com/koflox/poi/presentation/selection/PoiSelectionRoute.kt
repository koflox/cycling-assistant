package com.koflox.poi.presentation.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.designsystem.component.DebouncedButton
import com.koflox.designsystem.component.ReorderableColumn
import com.koflox.designsystem.component.SelectedItemRow
import com.koflox.designsystem.theme.Spacing
import com.koflox.poi.R
import com.koflox.poi.domain.model.MAX_SELECTED_POIS
import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.presentation.mapper.label

private const val CHIPS_PER_ROW = 2

@Composable
fun PoiSelectionRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PoiSelectionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                PoiSelectionNavigation.NavigateBack -> onBackClick()
            }
        }
    }
    PoiSelectionContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoiSelectionContent(
    uiState: PoiSelectionUiState,
    onEvent: (PoiSelectionUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.poi_selection_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.poi_selection_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (uiState is PoiSelectionUiState.Content) {
                Surface(tonalElevation = Spacing.Tiny) {
                    DebouncedButton(
                        onClick = { onEvent(PoiSelectionUiEvent.SaveClicked) },
                        enabled = uiState.isSaveEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.Large),
                    ) {
                        Text(stringResource(R.string.poi_selection_save))
                    }
                }
            }
        },
    ) { paddingValues ->
        when (uiState) {
            is PoiSelectionUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is PoiSelectionUiState.Content -> {
                PoiSelectionBody(
                    content = uiState,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun PoiSelectionBody(
    content: PoiSelectionUiState.Content,
    onEvent: (PoiSelectionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.poi_selection_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            val isValid = content.selectedPois.size == MAX_SELECTED_POIS
            Text(
                text = "${content.selectedPois.size}/$MAX_SELECTED_POIS",
                style = MaterialTheme.typography.labelLarge,
                color = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
            )
        }
        if (content.selectedPois.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.Medium))
            ReorderableColumn(
                items = content.selectedPois,
                key = { it.type.name },
                onReorder = { from, to -> onEvent(PoiSelectionUiEvent.PoiReordered(from, to)) },
                modifier = Modifier.padding(horizontal = Spacing.Large),
            ) { item, index, dragModifier ->
                SelectedItemRow(
                    index = index + 1,
                    label = item.type.label(),
                    onClick = { onEvent(PoiSelectionUiEvent.PoiRemoved(item.type)) },
                    dragHandleContentDescription = stringResource(R.string.poi_selection_drag_handle),
                    dragModifier = dragModifier,
                    modifier = Modifier.padding(vertical = Spacing.Tiny),
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.Large))
        Text(
            text = stringResource(R.string.poi_selection_available),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = Spacing.Large),
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        AvailablePoiChipsGrid(
            pois = content.availablePois,
            isAddEnabled = content.isAddEnabled,
            onPoiAdded = { onEvent(PoiSelectionUiEvent.PoiAdded(it)) },
            modifier = Modifier.padding(horizontal = Spacing.Large),
        )
        Spacer(modifier = Modifier.height(Spacing.Large))
    }
}

@Composable
private fun AvailablePoiChipsGrid(
    pois: List<PoiItemUiModel>,
    isAddEnabled: Boolean,
    onPoiAdded: (PoiType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        pois.chunked(CHIPS_PER_ROW).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
            ) {
                row.forEach { poi ->
                    FilterChip(
                        selected = false,
                        onClick = { onPoiAdded(poi.type) },
                        enabled = isAddEnabled,
                        label = {
                            Text(
                                text = poi.type.label(),
                                modifier = Modifier.padding(vertical = Spacing.Small),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(CHIPS_PER_ROW - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
