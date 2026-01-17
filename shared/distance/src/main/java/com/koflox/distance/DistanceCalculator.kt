package com.koflox.distance

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

interface DistanceCalculator {
    fun calculateKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double
}

internal class DefaultDistanceCalculator : DistanceCalculator {

    override fun calculateKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val sinHalfDLat = sin(dLat / 2)
        val sinHalfDLon = sin(dLon / 2)
        val cosLat1 = cos(Math.toRadians(lat1))
        val cosLat2 = cos(Math.toRadians(lat2))
        val a = sinHalfDLat * sinHalfDLat + cosLat1 * cosLat2 * sinHalfDLon * sinHalfDLon

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
    }
}
