package com.koflox.sensorprotocol.power

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface CyclingPowerParser {
    fun parse(data: ByteArray): CyclingPowerMeasurement?
}

fun CyclingPowerParser(): CyclingPowerParser = CyclingPowerParserImpl()

internal class CyclingPowerParserImpl : CyclingPowerParser {

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
     *
     * Optional fields preceding Crank Revolution Data are skipped based on their flags.
     * Unsigned fields use `and 0xFFFF` to prevent sign extension from [Short.toInt].
     *
     * @return parsed measurement, or `null` if [data] is too short (malformed BLE packet).
     */
    override fun parse(data: ByteArray): CyclingPowerMeasurement? {
        return try {
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            val flags = buffer.short.toInt() and 0xFFFF
            val instantaneousPower = buffer.short.toInt()
            skipOptionalFieldsBeforeCrankData(buffer, flags)
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
            CyclingPowerMeasurement(
                instantaneousPowerWatts = instantaneousPower,
                crankRevolutions = crankRevolutions,
                lastCrankEventTime = lastCrankEventTime,
            )
        } catch (_: BufferUnderflowException) {
            null
        }
    }

    private fun skipOptionalFieldsBeforeCrankData(buffer: ByteBuffer, flags: Int) {
        if (flags and CyclingPowerConstants.FLAG_PEDAL_POWER_BALANCE_PRESENT != 0) {
            buffer.position(buffer.position() + CyclingPowerConstants.PEDAL_POWER_BALANCE_SIZE)
        }
        if (flags and CyclingPowerConstants.FLAG_ACCUMULATED_TORQUE_PRESENT != 0) {
            buffer.position(buffer.position() + CyclingPowerConstants.ACCUMULATED_TORQUE_SIZE)
        }
        if (flags and CyclingPowerConstants.FLAG_WHEEL_REVOLUTION_DATA_PRESENT != 0) {
            buffer.position(buffer.position() + CyclingPowerConstants.WHEEL_REVOLUTION_DATA_SIZE)
        }
    }
}
