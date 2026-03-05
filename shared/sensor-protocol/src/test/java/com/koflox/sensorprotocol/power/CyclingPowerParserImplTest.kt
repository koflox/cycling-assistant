package com.koflox.sensorprotocol.power

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CyclingPowerParserImplTest {

    companion object {
        private const val POWER_150W = 150
        private const val POWER_0W = 0
        private const val CRANK_REVOLUTIONS = 42
        private const val LAST_CRANK_EVENT_TIME = 1024
        private const val PEDAL_POWER_BALANCE_RAW = 101
        private const val PEDAL_POWER_BALANCE_PERCENT = 50.5f
        private const val ACCUMULATED_TORQUE_RAW = 640
        private const val ACCUMULATED_TORQUE_NM = 20.0f
        private const val WHEEL_REVOLUTIONS = 1000L
        private const val LAST_WHEEL_EVENT_TIME = 4096
        private const val ACCUMULATED_ENERGY_KJ = 42
        private const val FLAGS_NO_CRANK = 0x0000
        private const val FLAGS_WITH_CRANK = 0x0020
        private const val FLAGS_ALL_OPTIONAL_AND_CRANK = 0x0035
        private const val FLAGS_ALL_PARSED_FIELDS = 0x0835
        private const val DELTA = 0.01f
    }

    private val parser: CyclingPowerParser = CyclingPowerParserImpl()

    @Test
    fun `parse power only without crank data`() {
        val data = createMeasurementData(
            flags = FLAGS_NO_CRANK,
            power = POWER_150W,
        )
        val result = parser.parse(data)!!
        assertEquals(POWER_150W, result.instantaneousPowerWatts)
        assertNull(result.crankRevolutions)
        assertNull(result.lastCrankEventTime)
        assertNull(result.pedalPowerBalancePercent)
        assertNull(result.accumulatedTorqueNm)
        assertNull(result.cumulativeWheelRevolutions)
        assertNull(result.lastWheelEventTime)
        assertNull(result.accumulatedEnergyKj)
    }

    @Test
    fun `parse power with crank data`() {
        val data = createMeasurementData(
            flags = FLAGS_WITH_CRANK,
            power = POWER_150W,
            crankRevolutions = CRANK_REVOLUTIONS,
            lastCrankEventTime = LAST_CRANK_EVENT_TIME,
        )
        val result = parser.parse(data)!!
        assertEquals(POWER_150W, result.instantaneousPowerWatts)
        assertNotNull(result.crankRevolutions)
        assertEquals(CRANK_REVOLUTIONS, result.crankRevolutions)
        assertNotNull(result.lastCrankEventTime)
        assertEquals(LAST_CRANK_EVENT_TIME, result.lastCrankEventTime)
    }

    @Test
    fun `parse zero power`() {
        val data = createMeasurementData(
            flags = FLAGS_NO_CRANK,
            power = POWER_0W,
        )
        val result = parser.parse(data)!!
        assertEquals(POWER_0W, result.instantaneousPowerWatts)
    }

    @Test
    fun `parse crank data with preceding optional fields`() {
        val data = createFullMeasurementData(
            flags = FLAGS_ALL_OPTIONAL_AND_CRANK,
            power = POWER_150W,
            pedalPowerBalanceRaw = PEDAL_POWER_BALANCE_RAW,
            accumulatedTorqueRaw = ACCUMULATED_TORQUE_RAW,
            wheelRevolutions = WHEEL_REVOLUTIONS,
            lastWheelEventTime = LAST_WHEEL_EVENT_TIME,
            crankRevolutions = CRANK_REVOLUTIONS,
            lastCrankEventTime = LAST_CRANK_EVENT_TIME,
        )
        val result = parser.parse(data)!!
        assertEquals(POWER_150W, result.instantaneousPowerWatts)
        assertEquals(CRANK_REVOLUTIONS, result.crankRevolutions)
        assertEquals(LAST_CRANK_EVENT_TIME, result.lastCrankEventTime)
        assertEquals(PEDAL_POWER_BALANCE_PERCENT, result.pedalPowerBalancePercent!!, DELTA)
        assertEquals(ACCUMULATED_TORQUE_NM, result.accumulatedTorqueNm!!, DELTA)
        assertEquals(WHEEL_REVOLUTIONS, result.cumulativeWheelRevolutions)
        assertEquals(LAST_WHEEL_EVENT_TIME, result.lastWheelEventTime)
    }

    @Test
    fun `parse pedal power balance`() {
        val flags = CyclingPowerConstants.FLAG_PEDAL_POWER_BALANCE_PRESENT
        val buffer = ByteBuffer.allocate(2 + 2 + 1).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(flags.toShort())
        buffer.putShort(POWER_150W.toShort())
        buffer.put(PEDAL_POWER_BALANCE_RAW.toByte())
        val result = parser.parse(buffer.array())!!
        assertEquals(PEDAL_POWER_BALANCE_PERCENT, result.pedalPowerBalancePercent!!, DELTA)
    }

    @Test
    fun `parse accumulated torque`() {
        val flags = CyclingPowerConstants.FLAG_ACCUMULATED_TORQUE_PRESENT
        val buffer = ByteBuffer.allocate(2 + 2 + 2).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(flags.toShort())
        buffer.putShort(POWER_150W.toShort())
        buffer.putShort(ACCUMULATED_TORQUE_RAW.toShort())
        val result = parser.parse(buffer.array())!!
        assertEquals(ACCUMULATED_TORQUE_NM, result.accumulatedTorqueNm!!, DELTA)
    }

    @Test
    fun `parse wheel revolution data`() {
        val flags = CyclingPowerConstants.FLAG_WHEEL_REVOLUTION_DATA_PRESENT
        val buffer = ByteBuffer.allocate(2 + 2 + 4 + 2).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(flags.toShort())
        buffer.putShort(POWER_150W.toShort())
        buffer.putInt(WHEEL_REVOLUTIONS.toInt())
        buffer.putShort(LAST_WHEEL_EVENT_TIME.toShort())
        val result = parser.parse(buffer.array())!!
        assertEquals(WHEEL_REVOLUTIONS, result.cumulativeWheelRevolutions)
        assertEquals(LAST_WHEEL_EVENT_TIME, result.lastWheelEventTime)
    }

    @Test
    fun `parse accumulated energy`() {
        val flags = CyclingPowerConstants.FLAG_ACCUMULATED_ENERGY_PRESENT
        val buffer = ByteBuffer.allocate(2 + 2 + 2).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(flags.toShort())
        buffer.putShort(POWER_150W.toShort())
        buffer.putShort(ACCUMULATED_ENERGY_KJ.toShort())
        val result = parser.parse(buffer.array())!!
        assertEquals(ACCUMULATED_ENERGY_KJ, result.accumulatedEnergyKj)
    }

    @Test
    fun `parse accumulated energy with skipped fields`() {
        val flags = FLAGS_ALL_PARSED_FIELDS or
            CyclingPowerConstants.FLAG_EXTREME_FORCE_PRESENT or
            CyclingPowerConstants.FLAG_TOP_DEAD_SPOT_PRESENT
        val buffer = ByteBuffer.allocate(
            2 + 2 + 1 + 2 + 6 + 4 +
                CyclingPowerConstants.EXTREME_FORCE_SIZE +
                CyclingPowerConstants.TOP_DEAD_SPOT_SIZE + 2,
        ).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(flags.toShort())
        buffer.putShort(POWER_150W.toShort())
        buffer.put(PEDAL_POWER_BALANCE_RAW.toByte())
        buffer.putShort(ACCUMULATED_TORQUE_RAW.toShort())
        buffer.putInt(WHEEL_REVOLUTIONS.toInt())
        buffer.putShort(LAST_WHEEL_EVENT_TIME.toShort())
        buffer.putShort(CRANK_REVOLUTIONS.toShort())
        buffer.putShort(LAST_CRANK_EVENT_TIME.toShort())
        repeat(CyclingPowerConstants.EXTREME_FORCE_SIZE) { buffer.put(0) }
        repeat(CyclingPowerConstants.TOP_DEAD_SPOT_SIZE) { buffer.put(0) }
        buffer.putShort(ACCUMULATED_ENERGY_KJ.toShort())
        val result = parser.parse(buffer.array())!!
        assertEquals(ACCUMULATED_ENERGY_KJ, result.accumulatedEnergyKj)
        assertEquals(CRANK_REVOLUTIONS, result.crankRevolutions)
    }

    @Test
    fun `parse malformed data returns null`() {
        val result = parser.parse(byteArrayOf(0x00))
        assertNull(result)
    }

    private fun createMeasurementData(
        flags: Int = FLAGS_NO_CRANK,
        power: Int = POWER_0W,
        crankRevolutions: Int = 0,
        lastCrankEventTime: Int = 0,
    ): ByteArray {
        val hasCrank = flags and CyclingPowerConstants.FLAG_CRANK_REVOLUTION_DATA_PRESENT != 0
        val size = if (hasCrank) 8 else 4
        val buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(flags.toShort())
        buffer.putShort(power.toShort())
        if (hasCrank) {
            buffer.putShort(crankRevolutions.toShort())
            buffer.putShort(lastCrankEventTime.toShort())
        }
        return buffer.array()
    }

    @Suppress("LongParameterList")
    private fun createFullMeasurementData(
        flags: Int,
        power: Int,
        pedalPowerBalanceRaw: Int = 0,
        accumulatedTorqueRaw: Int = 0,
        wheelRevolutions: Long = 0,
        lastWheelEventTime: Int = 0,
        crankRevolutions: Int = 0,
        lastCrankEventTime: Int = 0,
    ): ByteArray {
        var size = 2 + 2
        if (flags and CyclingPowerConstants.FLAG_PEDAL_POWER_BALANCE_PRESENT != 0) size += 1
        if (flags and CyclingPowerConstants.FLAG_ACCUMULATED_TORQUE_PRESENT != 0) size += 2
        if (flags and CyclingPowerConstants.FLAG_WHEEL_REVOLUTION_DATA_PRESENT != 0) size += 6
        if (flags and CyclingPowerConstants.FLAG_CRANK_REVOLUTION_DATA_PRESENT != 0) size += 4
        val buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(flags.toShort())
        buffer.putShort(power.toShort())
        if (flags and CyclingPowerConstants.FLAG_PEDAL_POWER_BALANCE_PRESENT != 0) {
            buffer.put(pedalPowerBalanceRaw.toByte())
        }
        if (flags and CyclingPowerConstants.FLAG_ACCUMULATED_TORQUE_PRESENT != 0) {
            buffer.putShort(accumulatedTorqueRaw.toShort())
        }
        if (flags and CyclingPowerConstants.FLAG_WHEEL_REVOLUTION_DATA_PRESENT != 0) {
            buffer.putInt(wheelRevolutions.toInt())
            buffer.putShort(lastWheelEventTime.toShort())
        }
        if (flags and CyclingPowerConstants.FLAG_CRANK_REVOLUTION_DATA_PRESENT != 0) {
            buffer.putShort(crankRevolutions.toShort())
            buffer.putShort(lastCrankEventTime.toShort())
        }
        return buffer.array()
    }
}
