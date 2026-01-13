package com.koflox.destinations.domain.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DistanceCalculator(
    private val dispatcherDefault: CoroutineDispatcher,
) {

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
    }

    suspend fun calculate(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double = withContext(dispatcherDefault) {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        EARTH_RADIUS_KM * c
    }

}
