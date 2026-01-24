package com.koflox.altitude

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultAltitudeCalculatorTest {

    private val calculator = DefaultAltitudeCalculator()

    @Test
    fun `calculateGain returns zero when previousAltitude is null`() {
        val gain = calculator.calculateGain(
            previousAltitude = null,
            currentAltitude = 100.0,
        )
        assertEquals(0.0, gain, 0.001)
    }

    @Test
    fun `calculateGain returns zero when currentAltitude is null`() {
        val gain = calculator.calculateGain(
            previousAltitude = 100.0,
            currentAltitude = null,
        )
        assertEquals(0.0, gain, 0.001)
    }

    @Test
    fun `calculateGain returns zero when both altitudes are null`() {
        val gain = calculator.calculateGain(
            previousAltitude = null,
            currentAltitude = null,
        )
        assertEquals(0.0, gain, 0.001)
    }

    @Test
    fun `calculateGain returns zero when altitude decreases`() {
        val gain = calculator.calculateGain(
            previousAltitude = 100.0,
            currentAltitude = 90.0,
        )
        assertEquals(0.0, gain, 0.001)
    }

    @Test
    fun `calculateGain returns zero when altitude stays the same`() {
        val gain = calculator.calculateGain(
            previousAltitude = 100.0,
            currentAltitude = 100.0,
        )
        assertEquals(0.0, gain, 0.001)
    }

    @Test
    fun `calculateGain returns zero when gain is below threshold`() {
        val gain = calculator.calculateGain(
            previousAltitude = 100.0,
            currentAltitude = 100.5,
        )
        assertEquals(0.0, gain, 0.001)
    }

    @Test
    fun `calculateGain returns zero when gain equals threshold`() {
        val gain = calculator.calculateGain(
            previousAltitude = 100.0,
            currentAltitude = 101.0,
        )
        assertEquals(0.0, gain, 0.001)
    }

    @Test
    fun `calculateGain returns gain when above threshold`() {
        val gain = calculator.calculateGain(
            previousAltitude = 100.0,
            currentAltitude = 105.0,
        )
        assertEquals(5.0, gain, 0.001)
    }

    @Test
    fun `calculateGain returns correct gain for large altitude change`() {
        val gain = calculator.calculateGain(
            previousAltitude = 500.0,
            currentAltitude = 650.0,
        )
        assertEquals(150.0, gain, 0.001)
    }

    @Test
    fun `calculateGain handles negative altitudes correctly`() {
        val gain = calculator.calculateGain(
            previousAltitude = -10.0,
            currentAltitude = 5.0,
        )
        assertEquals(15.0, gain, 0.001)
    }
}
