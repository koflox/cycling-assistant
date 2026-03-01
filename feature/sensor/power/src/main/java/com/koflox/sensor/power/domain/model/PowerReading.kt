package com.koflox.sensor.power.domain.model

data class PowerReading(
    val timestampMs: Long,
    val powerWatts: Int,
    val cadenceRpm: Float?,
)
