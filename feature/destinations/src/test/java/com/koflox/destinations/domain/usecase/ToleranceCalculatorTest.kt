package com.koflox.destinations.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ToleranceCalculatorTest {

    companion object {
        private const val DELTA = 0.01
    }

    private val calculator: ToleranceCalculator = ToleranceCalculatorImpl()

    @Test
    fun `1 km returns 0_5 km tolerance`() {
        assertEquals(0.5, calculator.calculateKm(1.0), DELTA)
    }

    @Test
    fun `5 km returns 1_9 km tolerance`() {
        assertEquals(1.9, calculator.calculateKm(5.0), DELTA)
    }

    @Test
    fun `10 km returns 3_2 km tolerance`() {
        assertEquals(3.2, calculator.calculateKm(10.0), DELTA)
    }

    @Test
    fun `50 km returns 9_4 km tolerance`() {
        assertEquals(9.4, calculator.calculateKm(50.0), DELTA)
    }

    @Test
    fun `150 km returns 15_0 km tolerance`() {
        assertEquals(15.0, calculator.calculateKm(150.0), DELTA)
    }

    @Test
    fun `distance below 1 km is clamped to 1 km`() {
        assertEquals(0.5, calculator.calculateKm(0.5), DELTA)
        assertEquals(0.5, calculator.calculateKm(0.0), DELTA)
    }
}
