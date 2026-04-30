package com.koflox.location.bearing

import com.koflox.location.model.Location
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class BearingCalculatorTest {

    companion object {
        private const val DELTA = 0.01f
        private const val ORIGIN_LAT = 50.0
        private const val ORIGIN_LON = 10.0
        private const val OFFSET = 1.0
    }

    private val origin = Location(latitude = ORIGIN_LAT, longitude = ORIGIN_LON)

    @Test
    fun `east bearing returns 0 degrees`() {
        val to = Location(latitude = ORIGIN_LAT, longitude = ORIGIN_LON + OFFSET)
        val bearing = calculateBearingDegrees(origin, to)
        assertEquals(0f, bearing, DELTA)
    }

    @Test
    fun `north bearing returns -90 degrees`() {
        val to = Location(latitude = ORIGIN_LAT + OFFSET, longitude = ORIGIN_LON)
        val bearing = calculateBearingDegrees(origin, to)
        assertEquals(-90f, bearing, DELTA)
    }

    @Test
    fun `south bearing returns 90 degrees`() {
        val to = Location(latitude = ORIGIN_LAT - OFFSET, longitude = ORIGIN_LON)
        val bearing = calculateBearingDegrees(origin, to)
        assertEquals(90f, bearing, DELTA)
    }

    @Test
    fun `west bearing returns minus 180 degrees`() {
        val to = Location(latitude = ORIGIN_LAT, longitude = ORIGIN_LON - OFFSET)
        val bearing = calculateBearingDegrees(origin, to)
        assertEquals(-180f, bearing, DELTA)
    }

    @Test
    fun `northeast bearing returns -45 degrees`() {
        val to = Location(latitude = ORIGIN_LAT + OFFSET, longitude = ORIGIN_LON + OFFSET)
        val bearing = calculateBearingDegrees(origin, to)
        assertEquals(-45f, bearing, DELTA)
    }

    @Test
    fun `identical points returns 0`() {
        val bearing = calculateBearingDegrees(origin, origin)
        assertEquals(0f, bearing, DELTA)
    }

    @Test
    fun `reverse direction differs by 180 degrees`() {
        val to = Location(latitude = ORIGIN_LAT + OFFSET, longitude = ORIGIN_LON + OFFSET)
        val forward = calculateBearingDegrees(origin, to)
        val reverse = calculateBearingDegrees(to, origin)
        val diff = abs(forward - reverse)
        assertTrue(abs(diff - 180f) < DELTA)
    }
}
