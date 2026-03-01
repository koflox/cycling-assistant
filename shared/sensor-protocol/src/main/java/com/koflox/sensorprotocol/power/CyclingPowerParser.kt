package com.koflox.sensorprotocol.power

import java.nio.ByteBuffer
import java.nio.ByteOrder

interface CyclingPowerParser {
    fun parse(data: ByteArray): CyclingPowerMeasurement
}

fun CyclingPowerParser(): CyclingPowerParser = CyclingPowerParserImpl()

internal class CyclingPowerParserImpl : CyclingPowerParser {

    override fun parse(data: ByteArray): CyclingPowerMeasurement {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val flags = buffer.short.toInt() and 0xFFFF
        val instantaneousPower = buffer.short.toInt()
        val hasCrankData = flags and CyclingPowerConstants.FLAG_CRANK_REVOLUTION_DATA_PRESENT != 0
        val crankRevolutions: Int?
        val lastCrankEventTime: Int?
        if (hasCrankData) {
            crankRevolutions = buffer.short.toInt() and 0xFFFF
            lastCrankEventTime = buffer.short.toInt() and 0xFFFF
        } else {
            crankRevolutions = null
            lastCrankEventTime = null
        }
        return CyclingPowerMeasurement(
            instantaneousPowerWatts = instantaneousPower,
            crankRevolutions = crankRevolutions,
            lastCrankEventTime = lastCrankEventTime,
        )
    }
}
