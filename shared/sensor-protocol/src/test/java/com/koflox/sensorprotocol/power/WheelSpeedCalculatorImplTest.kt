package com.koflox.sensorprotocol.power

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WheelSpeedCalculatorImplTest {

    companion object {
        private const val INITIAL_REVOLUTIONS = 100L
        private const val INITIAL_EVENT_TIME = 2048
        private const val SECOND_REVOLUTIONS = 110L
        private const val SECOND_EVENT_TIME = 4096
        private const val DELTA = 0.1f
    }

    private val calculator: WheelSpeedCalculator = WheelSpeedCalculatorImpl()

    @Test
    fun `first call returns null`() {
        val result = calculator.calculate(INITIAL_REVOLUTIONS, INITIAL_EVENT_TIME)
        assertNull(result)
    }

    @Test
    fun `second call returns speed`() {
        calculator.calculate(INITIAL_REVOLUTIONS, INITIAL_EVENT_TIME)
        val result = calculator.calculate(SECOND_REVOLUTIONS, SECOND_EVENT_TIME)
        // deltaRevs=10, deltaTime=2048, circumference=2.096
        // speed_m_s = 10 * 2.096 / (2048 / 2048) = 20.96
        // speed_km_h = 20.96 * 3.6 = 75.456
        assertEquals(75.5f, result!!, DELTA)
    }

    @Test
    fun `zero delta revolutions returns null`() {
        calculator.calculate(INITIAL_REVOLUTIONS, INITIAL_EVENT_TIME)
        val result = calculator.calculate(INITIAL_REVOLUTIONS, SECOND_EVENT_TIME)
        assertNull(result)
    }

    @Test
    fun `zero delta time returns null`() {
        calculator.calculate(INITIAL_REVOLUTIONS, INITIAL_EVENT_TIME)
        val result = calculator.calculate(SECOND_REVOLUTIONS, INITIAL_EVENT_TIME)
        assertNull(result)
    }

    @Test
    fun `wrap around wheel revolutions`() {
        val nearMax = 0xFFFFFFFFL - 4L
        calculator.calculate(nearMax, INITIAL_EVENT_TIME)
        val result = calculator.calculate(nearMax + 10L and 0xFFFFFFFFL, SECOND_EVENT_TIME)
        // deltaRevs=10, deltaTime=2048, same as second call test
        assertEquals(75.5f, result!!, DELTA)
    }

    @Test
    fun `wrap around event time`() {
        val nearMax = 0xFFFF - 1024
        calculator.calculate(INITIAL_REVOLUTIONS, nearMax)
        val result = calculator.calculate(SECOND_REVOLUTIONS, (nearMax + 2048) and 0xFFFF)
        // deltaRevs=10, deltaTime=2048, same as second call test
        assertEquals(75.5f, result!!, DELTA)
    }
}
