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
    }

    private val parser: CyclingPowerParser = CyclingPowerParserImpl()

    @Test
    fun `parse power only without crank data`() {
        val data = createMeasurementData(
            flags = FLAGS_NO_CRANK,
            power = POWER_150W,
        )
        val result = parser.parse(data)
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
        val result = parser.parse(data)
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
        val result = parser.parse(data)
        assertEquals(POWER_0W, result.instantaneousPowerWatts)
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
