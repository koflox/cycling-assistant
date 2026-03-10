package com.koflox.poi.presentation.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import com.koflox.designsystem.theme.ComponentSize
import com.koflox.designsystem.theme.Spacing
import com.koflox.poi.R
import com.koflox.poi.presentation.mapper.label

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
                PoiSelectionGrid(
                    pois = uiState.pois,
                    onPoiToggled = { onEvent(PoiSelectionUiEvent.PoiToggled(it)) },
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun PoiSelectionGrid(
    pois: List<PoiItemUiModel>,
    onPoiToggled: (com.koflox.poi.domain.model.PoiType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.poi_selection_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.Large),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(Spacing.Large),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
        ) {
            items(pois, key = { it.type.name }) { poi ->
                PoiFilterChip(
                    label = poi.type.label(),
                    isSelected = poi.isSelected,
                    selectionIndex = poi.selectionIndex,
                    onClick = { onPoiToggled(poi.type) },
                )
            }
        }
    }
}

@Composable
private fun PoiFilterChip(
    label: String,
    isSelected: Boolean,
    selectionIndex: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                modifier = Modifier.padding(vertical = Spacing.Small),
            )
        },
        trailingIcon = if (selectionIndex != null) {
            {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(ComponentSize.Badge),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = selectionIndex.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        } else {
            null
        },
        modifier = modifier.fillMaxWidth(),
    )
}
