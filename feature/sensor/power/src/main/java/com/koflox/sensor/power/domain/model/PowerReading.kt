package com.koflox.sensor.power.domain.model

data class PowerReading(
    val timestampMs: Long,
    val powerWatts: Int,
    val cadenceRpm: Float?,
    val pedalPowerBalancePercent: Float?,
    val accumulatedTorqueNm: Float?,
    val wheelSpeedKmh: Float?,
    val accumulatedEnergyKj: Int?,
)
