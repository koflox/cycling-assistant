package com.koflox.sensorprotocol.power

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface CyclingPowerParser {
    fun parse(data: ByteArray): CyclingPowerMeasurement?
}

fun CyclingPowerParser(): CyclingPowerParser = CyclingPowerParserImpl()

internal class CyclingPowerParserImpl : CyclingPowerParser {

    companion object {
        private const val PEDAL_POWER_BALANCE_DIVISOR = 2f
        private const val ACCUMULATED_TORQUE_DIVISOR = 32f
    }

    /**
     * Parses a raw Cycling Power Measurement characteristic value (UUID `0x2A63`).
     *
     * BLE GATT specifies multi-byte fields in **little-endian** byte order, so the buffer is
     * wrapped with [ByteOrder.LITTLE_ENDIAN] to read `short` values with the least significant
     * byte first (e.g., bytes `[0xE8, 0x03]` → `0x03E8` → 1000).
     *
     * Each [ByteBuffer.getShort] call advances the buffer position by 2 bytes, so sequential
     * reads produce different fields. Field order defined in GSS (section 3.65
     * "Cycling Power Measurement"):
     *
     * | Field                          | Type (per GSS) | Condition (Flags bit) |
     * |--------------------------------|----------------|-----------------------|
     * | Flags                          | `uint16`       | always                |
     * | Instantaneous Power            | `sint16`       | always                |
     * | Pedal Power Balance            | `uint8`        | bit 0                 |
     * | Accumulated Torque             | `uint16`       | bit 2                 |
     * | Wheel Revolution Data          | `uint32`+`uint16` | bit 4              |
     * | Crank Revolution Data          | `uint16`+`uint16` | bit 5              |
     * | Extreme Force Magnitudes       | 2×`sint16`     | bit 6 (skipped)       |
     * | Extreme Torque Magnitudes      | 2×`sint16`     | bit 7 (skipped)       |
     * | Extreme Angles                 | 3 bytes        | bit 8 (skipped)       |
     * | Top Dead Spot Angle            | `uint16`       | bit 9 (skipped)       |
     * | Bottom Dead Spot Angle         | `uint16`       | bit 10 (skipped)      |
     * | Accumulated Energy             | `uint16`       | bit 11                |
     *
     * Unsigned fields use `and 0xFFFF` / `and 0xFFFFFFFFL` to prevent sign extension.
     *
     * @return parsed measurement, or `null` if [data] is too short (malformed BLE packet).
     */
    override fun parse(data: ByteArray): CyclingPowerMeasurement? {
        return try {
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            val flags = buffer.short.toInt() and 0xFFFF
            val instantaneousPower = buffer.short.toInt()
            val pedalPowerBalance = parsePedalPowerBalance(buffer, flags)
            val accumulatedTorque = parseAccumulatedTorque(buffer, flags)
            val wheelData = parseWheelRevolutionData(buffer, flags)
            val crankData = parseCrankRevolutionData(buffer, flags)
            skipUnusedFields(buffer, flags)
            val accumulatedEnergy = parseAccumulatedEnergy(buffer, flags)
            CyclingPowerMeasurement(
                instantaneousPowerWatts = instantaneousPower,
                pedalPowerBalancePercent = pedalPowerBalance,
                accumulatedTorqueNm = accumulatedTorque,
                cumulativeWheelRevolutions = wheelData?.first,
                lastWheelEventTime = wheelData?.second,
                crankRevolutions = crankData?.first,
                lastCrankEventTime = crankData?.second,
                accumulatedEnergyKj = accumulatedEnergy,
            )
        } catch (_: BufferUnderflowException) {
            null
        }
    }

    private fun parsePedalPowerBalance(buffer: ByteBuffer, flags: Int): Float? {
        if (flags and CyclingPowerConstants.FLAG_PEDAL_POWER_BALANCE_PRESENT == 0) return null
        val raw = buffer.get().toInt() and 0xFF
        return raw / PEDAL_POWER_BALANCE_DIVISOR
    }

    private fun parseAccumulatedTorque(buffer: ByteBuffer, flags: Int): Float? {
        if (flags and CyclingPowerConstants.FLAG_ACCUMULATED_TORQUE_PRESENT == 0) return null
        val raw = buffer.short.toInt() and 0xFFFF
        return raw / ACCUMULATED_TORQUE_DIVISOR
    }

    private fun parseWheelRevolutionData(buffer: ByteBuffer, flags: Int): Pair<Long, Int>? {
        if (flags and CyclingPowerConstants.FLAG_WHEEL_REVOLUTION_DATA_PRESENT == 0) return null
        val revolutions = buffer.int.toLong() and 0xFFFFFFFFL
        val eventTime = buffer.short.toInt() and 0xFFFF
        return revolutions to eventTime
    }

    private fun parseCrankRevolutionData(buffer: ByteBuffer, flags: Int): Pair<Int, Int>? {
        if (flags and CyclingPowerConstants.FLAG_CRANK_REVOLUTION_DATA_PRESENT == 0) return null
        val revolutions = buffer.short.toInt() and 0xFFFF
        val eventTime = buffer.short.toInt() and 0xFFFF
        return revolutions to eventTime
    }

    private fun skipUnusedFields(buffer: ByteBuffer, flags: Int) {
        if (flags and CyclingPowerConstants.FLAG_EXTREME_FORCE_PRESENT != 0) {
            buffer.position(buffer.position() + CyclingPowerConstants.EXTREME_FORCE_SIZE)
        }
        if (flags and CyclingPowerConstants.FLAG_EXTREME_TORQUE_PRESENT != 0) {
            buffer.position(buffer.position() + CyclingPowerConstants.EXTREME_TORQUE_SIZE)
        }
        if (flags and CyclingPowerConstants.FLAG_EXTREME_ANGLES_PRESENT != 0) {
            buffer.position(buffer.position() + CyclingPowerConstants.EXTREME_ANGLES_SIZE)
        }
        if (flags and CyclingPowerConstants.FLAG_TOP_DEAD_SPOT_PRESENT != 0) {
            buffer.position(buffer.position() + CyclingPowerConstants.TOP_DEAD_SPOT_SIZE)
        }
        if (flags and CyclingPowerConstants.FLAG_BOTTOM_DEAD_SPOT_PRESENT != 0) {
            buffer.position(buffer.position() + CyclingPowerConstants.BOTTOM_DEAD_SPOT_SIZE)
        }
    }

    private fun parseAccumulatedEnergy(buffer: ByteBuffer, flags: Int): Int? {
        if (flags and CyclingPowerConstants.FLAG_ACCUMULATED_ENERGY_PRESENT == 0) return null
        return buffer.short.toInt() and 0xFFFF
    }
}
