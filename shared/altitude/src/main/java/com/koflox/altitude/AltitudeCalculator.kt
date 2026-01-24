package com.koflox.altitude

interface AltitudeCalculator {
    fun calculateGain(previousAltitude: Double?, currentAltitude: Double?): Double
}

internal class DefaultAltitudeCalculator : AltitudeCalculator {

    companion object {
        private const val MIN_ALTITUDE_GAIN_THRESHOLD_METERS = 1.0
    }

    override fun calculateGain(previousAltitude: Double?, currentAltitude: Double?): Double {
        if (previousAltitude == null || currentAltitude == null) {
            return 0.0
        }
        val altitudeDiff = currentAltitude - previousAltitude
        return if (altitudeDiff > MIN_ALTITUDE_GAIN_THRESHOLD_METERS) {
            altitudeDiff
        } else {
            0.0
        }
    }
}
