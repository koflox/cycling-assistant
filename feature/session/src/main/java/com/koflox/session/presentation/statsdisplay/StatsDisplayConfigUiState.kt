package com.koflox.session.presentation.statsdisplay

import com.koflox.session.domain.model.SessionStatType

internal sealed interface StatsDisplayConfigUiState {
    data object Loading : StatsDisplayConfigUiState
    data class Content(
        val sections: List<SectionUiModel>,
        val isSaveAllEnabled: Boolean,
    ) : StatsDisplayConfigUiState
}

internal data class SectionUiModel(
    val section: StatsDisplaySection,
    val stats: List<StatItemUiModel>,
    val isSaveEnabled: Boolean,
    val isSelectionValid: Boolean,
)

internal data class StatItemUiModel(
    val type: SessionStatType,
    val isSelected: Boolean,
    val selectionIndex: Int? = null,
)
