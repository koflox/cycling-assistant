package com.koflox.session.presentation.statsdisplay

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.designsystem.component.DebouncedButton
import com.koflox.designsystem.component.DebouncedOutlinedButton
import com.koflox.designsystem.component.ReorderableColumn
import com.koflox.designsystem.component.SelectedItemRow
import com.koflox.designsystem.testtag.TestTags
import com.koflox.designsystem.theme.Spacing
import com.koflox.session.R
import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.presentation.statsdisplay.components.StatsPreviewCard
import kotlinx.coroutines.launch

private const val CHIPS_PER_ROW = 2

@Composable
fun StatsDisplayConfigRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialSection: StatsDisplaySection? = null,
) {
    val viewModel: StatsDisplayConfigViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { event ->
            when (event) {
                StatsDisplayConfigNavigation.NavigateBack -> onBackClick()
            }
        }
    }
    StatsDisplayConfigContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        initialSection = initialSection,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsDisplayConfigContent(
    uiState: StatsDisplayConfigUiState,
    onEvent: (StatsDisplayConfigUiEvent) -> Unit,
    onBackClick: () -> Unit,
    initialSection: StatsDisplaySection?,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag(TestTags.STATS_CONFIG_SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_config_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.stats_config_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when (uiState) {
            StatsDisplayConfigUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is StatsDisplayConfigUiState.Content -> {
                SectionsColumn(
                    content = uiState,
                    onEvent = onEvent,
                    initialSection = initialSection,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun SectionsColumn(
    content: StatsDisplayConfigUiState.Content,
    onEvent: (StatsDisplayConfigUiEvent) -> Unit,
    initialSection: StatsDisplaySection?,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val sectionPositions = mutableMapOf<StatsDisplaySection, Float>()
    var hasScrolledToSection by remember { mutableStateOf(false) }
    LaunchedEffect(initialSection, content) {
        if (initialSection != null && !hasScrolledToSection) {
            val position = sectionPositions[initialSection]
            if (position != null) {
                hasScrolledToSection = true
                coroutineScope.launch {
                    scrollState.animateScrollTo(position.toInt())
                }
            }
        }
    }
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = Spacing.Large)
            .testTag(TestTags.STATS_CONFIG_SCROLL),
    ) {
        content.sections.forEach { section ->
            SectionBlock(
                section = section,
                onEvent = onEvent,
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    sectionPositions[section.section] = coordinates.positionInParent().y
                },
            )
            Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
        }
        DebouncedButton(
            onClick = { onEvent(StatsDisplayConfigUiEvent.SaveAllClicked) },
            enabled = content.isSaveAllEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.stats_config_save_all))
        }
        Spacer(modifier = Modifier.height(Spacing.Large))
    }
}

@Composable
private fun SectionBlock(
    section: SectionUiModel,
    onEvent: (StatsDisplayConfigUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(section = section)
        if (section.selectedStats.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.Medium))
            ReorderableColumn(
                items = section.selectedStats,
                key = { it.type.name },
                onReorder = { from, to ->
                    onEvent(StatsDisplayConfigUiEvent.StatReordered(section.section, from, to))
                },
                modifier = if (section.section == StatsDisplaySection.ACTIVE_SESSION) {
                    Modifier.testTag(TestTags.STATS_CONFIG_SELECTED_LIST)
                } else {
                    Modifier
                },
            ) { item, index, dragModifier ->
                SelectedItemRow(
                    index = index + 1,
                    label = stringResource(statTypeToLabelRes(item.type)),
                    onClick = {
                        onEvent(StatsDisplayConfigUiEvent.StatRemoved(section.section, item.type))
                    },
                    dragHandleContentDescription = stringResource(R.string.stats_config_drag_handle),
                    dragModifier = dragModifier,
                    modifier = Modifier.padding(vertical = Spacing.Tiny),
                )
            }
        }
        if (section.availableStats.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.Large))
            Text(
                text = stringResource(R.string.stats_config_available),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(Spacing.Small))
            AvailableStatChipsGrid(
                stats = section.availableStats,
                isAddEnabled = section.isAddEnabled,
                onStatAdded = { type ->
                    onEvent(StatsDisplayConfigUiEvent.StatAdded(section.section, type))
                },
            )
        }
        Spacer(modifier = Modifier.height(Spacing.Medium))
        StatsPreviewCard(
            section = section.section,
            selectedStats = section.selectedStats.map { it.type },
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        SectionActionButtons(section = section, onEvent = onEvent)
    }
}

@Composable
private fun SectionHeader(
    section: SectionUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(sectionToTitleRes(section.section)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            val counterText = if (section.maxSelectionCount != null) {
                "${section.selectedStats.size}/${section.maxSelectionCount}"
            } else {
                "${section.selectedStats.size}"
            }
            Text(
                text = counterText,
                style = MaterialTheme.typography.labelLarge,
                color = if (section.isSelectionValid) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(Spacing.Tiny))
        Text(
            text = stringResource(sectionToConstraintRes(section.section)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionActionButtons(
    section: SectionUiModel,
    onEvent: (StatsDisplayConfigUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        DebouncedOutlinedButton(
            onClick = { onEvent(StatsDisplayConfigUiEvent.ResetSectionClicked(section.section)) },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.stats_config_reset_section))
        }
        DebouncedOutlinedButton(
            onClick = { onEvent(StatsDisplayConfigUiEvent.SaveSectionClicked(section.section)) },
            enabled = section.isSaveEnabled,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.stats_config_save_section))
        }
    }
}

@Composable
private fun AvailableStatChipsGrid(
    stats: List<StatItemUiModel>,
    isAddEnabled: Boolean,
    onStatAdded: (SessionStatType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        stats.chunked(CHIPS_PER_ROW).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
            ) {
                row.forEach { stat ->
                    FilterChip(
                        selected = false,
                        onClick = { onStatAdded(stat.type) },
                        enabled = isAddEnabled,
                        label = {
                            Text(
                                text = stringResource(statTypeToLabelRes(stat.type)),
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
