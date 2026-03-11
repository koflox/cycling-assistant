package com.koflox.session.presentation.statsdisplay

import com.koflox.session.domain.model.SessionStatType

internal sealed interface StatsDisplayConfigUiEvent {
    data class StatAdded(
        val section: StatsDisplaySection,
        val type: SessionStatType,
    ) : StatsDisplayConfigUiEvent
    data class StatRemoved(
        val section: StatsDisplaySection,
        val type: SessionStatType,
    ) : StatsDisplayConfigUiEvent
    data class StatReordered(
        val section: StatsDisplaySection,
        val fromIndex: Int,
        val toIndex: Int,
    ) : StatsDisplayConfigUiEvent
    data class ResetSectionClicked(val section: StatsDisplaySection) : StatsDisplayConfigUiEvent
    data class SaveSectionClicked(val section: StatsDisplaySection) : StatsDisplayConfigUiEvent
    data object SaveAllClicked : StatsDisplayConfigUiEvent
}
