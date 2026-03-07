package com.koflox.sensorprotocol.power

data class CyclingPowerMeasurement(
    val instantaneousPowerWatts: Int,
    val pedalPowerBalancePercent: Float?,
    val accumulatedTorqueNm: Float?,
    val cumulativeWheelRevolutions: Long?,
    val lastWheelEventTime: Int?,
    val crankRevolutions: Int?,
    val lastCrankEventTime: Int?,
    val accumulatedEnergyKj: Int?,
)
