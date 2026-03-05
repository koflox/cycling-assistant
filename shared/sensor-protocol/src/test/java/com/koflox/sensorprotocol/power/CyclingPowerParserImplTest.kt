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
        private const val FLAGS_NO_CRANK = 0x0000
        private const val FLAGS_WITH_CRANK = 0x0020
        private const val FLAGS_ALL_OPTIONAL_AND_CRANK = 0x0035
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
    fun `parse crank data with preceding optional fields skipped`() {
        val flags = FLAGS_ALL_OPTIONAL_AND_CRANK
        val buffer = ByteBuffer.allocate(
            2 + 2 + CyclingPowerConstants.PEDAL_POWER_BALANCE_SIZE +
                CyclingPowerConstants.ACCUMULATED_TORQUE_SIZE +
                CyclingPowerConstants.WHEEL_REVOLUTION_DATA_SIZE + 2 + 2,
        ).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(flags.toShort())
        buffer.putShort(POWER_150W.toShort())
        buffer.put(50.toByte())
        buffer.putShort(0x1234.toShort())
        buffer.putInt(100)
        buffer.putShort(0x5678.toShort())
        buffer.putShort(CRANK_REVOLUTIONS.toShort())
        buffer.putShort(LAST_CRANK_EVENT_TIME.toShort())
        val result = parser.parse(buffer.array())!!
        assertEquals(POWER_150W, result.instantaneousPowerWatts)
        assertEquals(CRANK_REVOLUTIONS, result.crankRevolutions)
        assertEquals(LAST_CRANK_EVENT_TIME, result.lastCrankEventTime)
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
}
