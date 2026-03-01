package com.koflox.session.presentation.mapper

import android.content.Context
import com.koflox.designsystem.context.LocalizedContextProvider
import com.koflox.session.R
import com.koflox.session.domain.model.Session
import com.koflox.session.domain.model.SessionDerivedStats
import com.koflox.session.domain.model.SessionStatType
import com.koflox.session.presentation.model.DisplayStat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class SessionUiMapperImpl(
    private val localizedContextProvider: LocalizedContextProvider,
) : SessionUiMapper {

    companion object {
        private const val DATE_FORMAT_PATTERN = "MMM dd, yyyy HH:mm"
        private const val FORMAT_WHOLE_NUMBER = "%.0f"
        private const val UNAVAILABLE_VALUE = "---"
        private const val JOULES_PER_KCAL = 1000.0
    }

    private val appLocale: Locale
        get() = localizedContextProvider.getLocalizedContext().resources.configuration.locales[0]

    override fun formatElapsedTime(elapsedMs: Long): String {
        val totalSeconds = elapsedMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(appLocale, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun formatDistance(distanceKm: Double): String =
        String.format(appLocale, "%.2f", distanceKm)

    override fun formatSpeed(speedKmh: Double): String =
        String.format(appLocale, "%.1f", speedKmh)

    override fun formatAltitudeGain(altitudeGainMeters: Double): String =
        String.format(appLocale, FORMAT_WHOLE_NUMBER, altitudeGainMeters)

    override fun formatCalories(calories: Double): String =
        String.format(appLocale, FORMAT_WHOLE_NUMBER, calories)

    override fun formatPower(powerWatts: Int): String = powerWatts.toString()

    override fun formatStartDate(startTimeMs: Long): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT_PATTERN, appLocale)
        return dateFormat.format(Date(startTimeMs))
    }

    override fun toSessionUiModel(session: Session): SessionUiModel = SessionUiModel(
        elapsedTimeFormatted = formatElapsedTime(session.elapsedTimeMs),
        traveledDistanceFormatted = formatDistance(session.traveledDistanceKm),
        averageSpeedFormatted = formatSpeed(session.averageSpeedKmh),
        topSpeedFormatted = formatSpeed(session.topSpeedKmh),
        altitudeGainFormatted = formatAltitudeGain(session.totalAltitudeGainMeters),
    )

    override fun buildActiveSessionStats(
        session: Session,
        statTypes: List<SessionStatType>,
    ): List<DisplayStat> {
        val context = localizedContextProvider.getLocalizedContext()
        return statTypes.map { type ->
            val label = context.getString(statTypeToLabelRes(type))
            val value = formatActiveStatValue(context, type, session)
            DisplayStat(label = label, value = value)
        }
    }

    override fun buildCompletedSessionStats(
        session: Session,
        derivedStats: SessionDerivedStats,
        statTypes: List<SessionStatType>,
    ): List<DisplayStat> {
        val context = localizedContextProvider.getLocalizedContext()
        return statTypes.map { type ->
            val label = context.getString(statTypeToLabelRes(type))
            val value = formatCompletedStatValue(context, type, session, derivedStats)
            DisplayStat(label = label, value = value)
        }
    }

    private fun statTypeToLabelRes(type: SessionStatType): Int = when (type) {
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

    private fun formatActiveStatValue(
        context: Context,
        type: SessionStatType,
        session: Session,
    ): String = when (type) {
        SessionStatType.TIME -> formatElapsedTime(session.elapsedTimeMs)
        SessionStatType.DISTANCE -> context.getString(R.string.session_stat_value_km, formatDistance(session.traveledDistanceKm))
        SessionStatType.AVG_SPEED -> context.getString(R.string.session_stat_value_kmh, formatSpeed(session.averageSpeedKmh))
        SessionStatType.TOP_SPEED -> context.getString(R.string.session_stat_value_kmh, formatSpeed(session.topSpeedKmh))
        SessionStatType.ALTITUDE_GAIN -> context.getString(
            R.string.session_stat_value_m,
            formatAltitudeGain(session.totalAltitudeGainMeters),
        )
        SessionStatType.AVG_POWER -> session.averagePowerWatts?.let {
            context.getString(R.string.session_stat_value_w, formatPower(it))
        } ?: UNAVAILABLE_VALUE
        SessionStatType.MAX_POWER -> session.maxPowerWatts?.let {
            context.getString(R.string.session_stat_value_w, formatPower(it))
        } ?: UNAVAILABLE_VALUE
        SessionStatType.CALORIES_POWER -> session.totalEnergyJoules?.let {
            context.getString(R.string.session_stat_value_kcal, formatCalories(it / JOULES_PER_KCAL))
        } ?: UNAVAILABLE_VALUE
        else -> UNAVAILABLE_VALUE
    }

    @Suppress("CyclomaticComplexity")
    private fun formatCompletedStatValue(
        context: Context,
        type: SessionStatType,
        session: Session,
        derivedStats: SessionDerivedStats,
    ): String = when (type) {
        SessionStatType.TIME -> formatElapsedTime(session.elapsedTimeMs)
        SessionStatType.DISTANCE -> context.getString(R.string.session_stat_value_km, formatDistance(session.traveledDistanceKm))
        SessionStatType.AVG_SPEED -> context.getString(R.string.session_stat_value_kmh, formatSpeed(session.averageSpeedKmh))
        SessionStatType.TOP_SPEED -> context.getString(R.string.session_stat_value_kmh, formatSpeed(session.topSpeedKmh))
        SessionStatType.ALTITUDE_GAIN -> context.getString(
            R.string.session_stat_value_m,
            formatAltitudeGain(session.totalAltitudeGainMeters),
        )
        SessionStatType.ALTITUDE_LOSS -> context.getString(
            R.string.session_stat_value_m,
            formatAltitudeGain(derivedStats.altitudeLossMeters),
        )
        SessionStatType.MOVING_TIME -> formatElapsedTime(derivedStats.movingTimeMs)
        SessionStatType.IDLE_TIME -> formatElapsedTime(derivedStats.idleTimeMs)
        SessionStatType.AVG_POWER -> session.averagePowerWatts?.let {
            context.getString(R.string.session_stat_value_w, formatPower(it))
        } ?: UNAVAILABLE_VALUE
        SessionStatType.MAX_POWER -> session.maxPowerWatts?.let {
            context.getString(R.string.session_stat_value_w, formatPower(it))
        } ?: UNAVAILABLE_VALUE
        SessionStatType.CALORIES_WEIGHT -> derivedStats.caloriesBurned?.let {
            context.getString(R.string.session_stat_value_kcal, formatCalories(it))
        } ?: UNAVAILABLE_VALUE
        SessionStatType.CALORIES_POWER -> if (session.hasPowerData && session.totalEnergyJoules != null) {
            context.getString(R.string.session_stat_value_kcal, formatCalories(session.totalEnergyJoules / JOULES_PER_KCAL))
        } else {
            UNAVAILABLE_VALUE
        }
    }
}
