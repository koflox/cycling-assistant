package com.koflox.session.presentation.statsdisplay

internal sealed interface StatsDisplayConfigNavigation {
    data object NavigateBack : StatsDisplayConfigNavigation
}
