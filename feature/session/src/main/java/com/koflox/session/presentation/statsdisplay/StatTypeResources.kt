package com.koflox.session.presentation.statsdisplay

import com.koflox.session.R
import com.koflox.session.domain.model.SessionStatType

internal fun statTypeToLabelRes(type: SessionStatType): Int = when (type) {
    SessionStatType.TIME -> R.string.session_stat_time
    SessionStatType.DISTANCE -> R.string.session_stat_distance
    SessionStatType.AVG_SPEED -> R.string.session_stat_avg_speed
    SessionStatType.TOP_SPEED -> R.string.session_stat_top_speed
    SessionStatType.ALTITUDE_GAIN -> R.string.session_stat_altitude_gain
    SessionStatType.ALTITUDE_LOSS -> R.string.session_stat_altitude_loss
    SessionStatType.MOVING_TIME -> R.string.session_stat_moving_time
    SessionStatType.IDLE_TIME -> R.string.session_stat_idle_time
    SessionStatType.AVG_POWER -> R.string.session_stat_avg_power
    SessionStatType.MAX_POWER -> R.string.session_stat_max_power
    SessionStatType.CALORIES_WEIGHT -> R.string.session_stat_calories_weight
    SessionStatType.CALORIES_POWER -> R.string.session_stat_calories_power
}

internal fun sectionToTitleRes(section: StatsDisplaySection): Int = when (section) {
    StatsDisplaySection.ACTIVE_SESSION -> R.string.stats_config_section_active
    StatsDisplaySection.COMPLETED_SESSION -> R.string.stats_config_section_completed
    StatsDisplaySection.SHARE -> R.string.stats_config_section_share
}

internal fun sectionToConstraintRes(section: StatsDisplaySection): Int = when (section) {
    StatsDisplaySection.ACTIVE_SESSION -> R.string.stats_config_hint_active
    StatsDisplaySection.COMPLETED_SESSION -> R.string.stats_config_hint_completed
    StatsDisplaySection.SHARE -> R.string.stats_config_hint_share
}
