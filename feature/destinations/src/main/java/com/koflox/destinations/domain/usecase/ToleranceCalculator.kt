package com.koflox.destinations.domain.usecase

import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Computes the search tolerance (+-) range around a target distance using a logarithmic curve.
 *
 * Shorter distances get a proportionally wider tolerance (more candidates in a dense city center),
 * while longer distances use a tighter percentage to avoid overly broad searches.
 *
 * | Distance | Tolerance percent | Tolerance |
 * |----------|-------------------|-----------|
 * | 1 km     | 50%               | +-0.5 km  |
 * | 5 km     | 37%               | +-1.9 km  |
 * | 10 km    | 32%               | +-3.2 km  |
 * | 50 km    | 19%               | +-9.4 km  |
 * | 150 km   | 10%               | +-15.0 km |
 */
internal interface ToleranceCalculator {

    /**
     * @param distanceKm target distance in kilometres (clamped to a minimum of 1 km).
     * @return tolerance in kilometres, rounded to one decimal place.
     */
    fun calculateKm(distanceKm: Double): Double
}

internal class ToleranceCalculatorImpl : ToleranceCalculator {

    companion object {
        private const val MIN_TOLERANCE_PERCENT = 10.0
        private const val MAX_TOLERANCE_PERCENT = 50.0
        private const val MAX_DISTANCE_KM = 150.0
    }

    override fun calculateKm(distanceKm: Double): Double {
        val clamped = distanceKm.coerceAtLeast(1.0)
        val percent = MIN_TOLERANCE_PERCENT + (MAX_TOLERANCE_PERCENT - MIN_TOLERANCE_PERCENT) *
            (1 - ln(clamped) / ln(MAX_DISTANCE_KM))
        return (clamped * percent / 100.0 * 10).roundToInt() / 10.0
    }
}
