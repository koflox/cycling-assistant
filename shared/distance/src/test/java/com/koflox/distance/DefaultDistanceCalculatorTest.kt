package com.koflox.distance

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDistanceCalculatorTest {

    private val calculator = DefaultDistanceCalculator()

    @Test
    fun `calculateKm returns zero for same coordinates`() {
        val distance = calculator.calculateKm(
            lat1 = 52.52,
            lon1 = 13.405,
            lat2 = 52.52,
            lon2 = 13.405,
        )
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `calculateKm returns correct distance between Berlin and Munich`() {
        // Berlin: 52.52, 13.405
        // Munich: 48.1351, 11.5820
        // Expected distance: ~504 km
        val distance = calculator.calculateKm(
            lat1 = 52.52,
            lon1 = 13.405,
            lat2 = 48.1351,
            lon2 = 11.5820,
        )
        assertEquals(504.0, distance, 5.0)
    }

    @Test
    fun `calculateKm returns correct distance between New York and Los Angeles`() {
        // New York: 40.7128, -74.0060
        // Los Angeles: 34.0522, -118.2437
        // Expected distance: ~3940 km
        val distance = calculator.calculateKm(
            lat1 = 40.7128,
            lon1 = -74.0060,
            lat2 = 34.0522,
            lon2 = -118.2437,
        )
        assertEquals(3940.0, distance, 20.0)
    }

    @Test
    fun `calculateKm returns correct short distance`() {
        // Two points approximately 10km apart in Berlin area
        // Start: 52.52, 13.405
        // End: 52.61, 13.405 (roughly 10km north)
        val distance = calculator.calculateKm(
            lat1 = 52.52,
            lon1 = 13.405,
            lat2 = 52.61,
            lon2 = 13.405,
        )
        assertEquals(10.0, distance, 1.0)
    }

    @Test
    fun `calculateKm handles negative coordinates`() {
        // Sydney: -33.8688, 151.2093
        // Auckland: -36.8509, 174.7645
        // Expected distance: ~2155 km
        val distance = calculator.calculateKm(
            lat1 = -33.8688,
            lon1 = 151.2093,
            lat2 = -36.8509,
            lon2 = 174.7645,
        )
        assertEquals(2155.0, distance, 20.0)
    }

    @Test
    fun `calculateKm is symmetric`() {
        val distance1 = calculator.calculateKm(
            lat1 = 52.52,
            lon1 = 13.405,
            lat2 = 48.1351,
            lon2 = 11.5820,
        )
        val distance2 = calculator.calculateKm(
            lat1 = 48.1351,
            lon1 = 11.5820,
            lat2 = 52.52,
            lon2 = 13.405,
        )
        assertEquals(distance1, distance2, 0.001)
    }
}
