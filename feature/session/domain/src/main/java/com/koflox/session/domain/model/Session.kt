package com.koflox.session.domain.model

data class Session(
    val id: String,
    val destinationId: String?,
    val destinationName: String?,
    val destinationLatitude: Double?,
    val destinationLongitude: Double?,
    val startLatitude: Double,
    val startLongitude: Double,
    val startTimeMs: Long,
    val lastResumedTimeMs: Long,
    val endTimeMs: Long?,
    val elapsedTimeMs: Long,
    val traveledDistanceKm: Double,
    val averageSpeedKmh: Double,
    val topSpeedKmh: Double,
    val totalAltitudeGainMeters: Double,
    val status: SessionStatus,
    val trackPoints: List<TrackPoint>,
    val totalPowerReadings: Int? = null,
    val sumPowerWatts: Long? = null,
    val maxPowerWatts: Int? = null,
    val totalEnergyJoules: Double? = null,
) {
    val averagePowerWatts: Int?
        get() {
            val readings = totalPowerReadings ?: return null
            val sum = sumPowerWatts ?: return null
            if (readings == 0) return null
            return (sum / readings).toInt()
        }
    val hasPowerData: Boolean get() = totalPowerReadings != null && totalPowerReadings > 0
}
