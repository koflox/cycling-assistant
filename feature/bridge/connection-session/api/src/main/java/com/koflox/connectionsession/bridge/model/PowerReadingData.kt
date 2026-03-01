package com.koflox.connectionsession.bridge.model

data class PowerReadingData(
    val timestampMs: Long,
    val powerWatts: Int,
    val cadenceRpm: Float?,
)
