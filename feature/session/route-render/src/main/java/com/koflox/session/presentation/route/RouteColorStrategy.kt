package com.koflox.session.presentation.route

import androidx.compose.ui.graphics.toArgb
import com.koflox.map.RouteColors
import com.koflox.session.presentation.route.SpeedRouteColorStrategy.Companion.SPEED_THRESHOLD_FAST

internal fun interface RouteColorStrategy {
    fun assignColors(points: List<RoutePoint>): List<Int>
}

/**
 * Assigns a uniform color to all route points, ignoring speed and power data.
 */
internal class DefaultRouteColorStrategy : RouteColorStrategy {

    companion object {
        private val DEFAULT_COLOR_ARGB = RouteColors.NormalSpeed.toArgb()
    }

    override fun assignColors(points: List<RoutePoint>): List<Int> =
        List(points.size) { DEFAULT_COLOR_ARGB }
}

/**
 * Colors route points based on speed using a sliding-window average.
 * Points below [SPEED_THRESHOLD_FAST] km/h get the normal color, at or above get the fast color.
 */
internal class SpeedRouteColorStrategy : RouteColorStrategy {

    companion object {
        private const val SPEED_THRESHOLD_FAST = 30.0
        private const val SPEED_SMOOTHING_WINDOW = 5
        private val SPEED_COLOR_NORMAL_ARGB = RouteColors.NormalSpeed.toArgb()
        private val SPEED_COLOR_FAST_ARGB = RouteColors.FastSpeed.toArgb()
    }

    override fun assignColors(points: List<RoutePoint>): List<Int> {
        val halfWindow = SPEED_SMOOTHING_WINDOW / 2
        return List(points.size) { i ->
            val start = maxOf(0, i - halfWindow)
            val end = minOf(points.size, i + halfWindow + 1)
            var sum = 0.0
            for (j in start until end) {
                sum += points[j].speedKmh
            }
            speedToColor(sum / (end - start))
        }
    }

    private fun speedToColor(speedKmh: Double): Int =
        if (speedKmh < SPEED_THRESHOLD_FAST) SPEED_COLOR_NORMAL_ARGB else SPEED_COLOR_FAST_ARGB
}

/**
 * Colors route points based on power output using a sliding-window average.
 * Maps to three zones: low (< 150 W), medium (150–250 W), high (>= 250 W).
 * Points with null power are treated as zero.
 */
internal class PowerRouteColorStrategy : RouteColorStrategy {

    companion object {
        private const val POWER_THRESHOLD_MEDIUM = 150
        private const val POWER_THRESHOLD_HIGH = 250
        private const val POWER_SMOOTHING_WINDOW = 5
        private val LOW_POWER_COLOR_ARGB = RouteColors.LowPower.toArgb()
        private val MEDIUM_POWER_COLOR_ARGB = RouteColors.MediumPower.toArgb()
        private val HIGH_POWER_COLOR_ARGB = RouteColors.HighPower.toArgb()
    }

    override fun assignColors(points: List<RoutePoint>): List<Int> {
        val halfWindow = POWER_SMOOTHING_WINDOW / 2
        return List(points.size) { i ->
            val start = maxOf(0, i - halfWindow)
            val end = minOf(points.size, i + halfWindow + 1)
            var sum = 0.0
            var count = 0
            for (j in start until end) {
                val power = points[j].powerWatts
                if (power != null) {
                    sum += power
                    count++
                }
            }
            val avgPower = if (count > 0) sum / count else 0.0
            powerToColor(avgPower)
        }
    }

    private fun powerToColor(power: Double): Int = when {
        power >= POWER_THRESHOLD_HIGH -> HIGH_POWER_COLOR_ARGB
        power >= POWER_THRESHOLD_MEDIUM -> MEDIUM_POWER_COLOR_ARGB
        else -> LOW_POWER_COLOR_ARGB
    }
}
