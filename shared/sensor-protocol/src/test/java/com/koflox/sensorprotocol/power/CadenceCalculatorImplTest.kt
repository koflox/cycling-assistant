package com.koflox.sensorprotocol.power

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CadenceCalculatorImplTest {

    companion object {
        private const val INITIAL_REVOLUTIONS = 100
        private const val INITIAL_EVENT_TIME = 1024
        private const val SECOND_REVOLUTIONS = 101
        private const val SECOND_EVENT_TIME = 2048
        private const val DELTA = 0.01f
    }

    private val calculator: CadenceCalculator = CadenceCalculatorImpl()

    @Test
    fun `first call returns null`() {
        val result = calculator.calculate(INITIAL_REVOLUTIONS, INITIAL_EVENT_TIME)
        assertNull(result)
    }

    @Test
    fun `second call returns cadence`() {
        calculator.calculate(INITIAL_REVOLUTIONS, INITIAL_EVENT_TIME)
        val result = calculator.calculate(SECOND_REVOLUTIONS, SECOND_EVENT_TIME)
        // deltaRevs=1, deltaTime=1024, cadence = 1/1024 * 60 * 1024 = 60
        assertEquals(60f, result!!, DELTA)
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
    fun `multiple revolutions per interval`() {
        calculator.calculate(INITIAL_REVOLUTIONS, INITIAL_EVENT_TIME)
        val result = calculator.calculate(INITIAL_REVOLUTIONS + 2, SECOND_EVENT_TIME)
        // deltaRevs=2, deltaTime=1024, cadence = 2/1024 * 60 * 1024 = 120
        assertEquals(120f, result!!, DELTA)
    }
}
