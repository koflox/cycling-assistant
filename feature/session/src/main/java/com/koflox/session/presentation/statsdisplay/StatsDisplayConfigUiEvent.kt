package com.koflox.session.presentation.statsdisplay

import com.koflox.session.domain.model.SessionStatType

internal sealed interface StatsDisplayConfigUiEvent {
    data class StatToggled(
        val section: StatsDisplaySection,
        val type: SessionStatType,
    ) : StatsDisplayConfigUiEvent
    data class ResetSectionClicked(val section: StatsDisplaySection) : StatsDisplayConfigUiEvent
    data class SaveSectionClicked(val section: StatsDisplaySection) : StatsDisplayConfigUiEvent
    data object SaveAllClicked : StatsDisplayConfigUiEvent
}
