package com.koflox.sensorprotocol.power

data class CyclingPowerMeasurement(
    val instantaneousPowerWatts: Int,
    val crankRevolutions: Int?,
    val lastCrankEventTime: Int?,
)
