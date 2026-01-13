package com.koflox.destinations.domain.util

import com.koflox.testing.coroutine.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DistanceCalculatorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val calculator = DistanceCalculator(mainDispatcherRule.testDispatcher)

    @Test
    fun `calculate returns zero for same coordinates`() = runTest {
        val distance = calculator.calculate(
            lat1 = 52.52,
            lon1 = 13.405,
            lat2 = 52.52,
            lon2 = 13.405,
        )
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `calculate returns correct distance between Berlin and Munich`() = runTest {
        // Berlin: 52.52, 13.405
        // Munich: 48.1351, 11.5820
        // Expected distance: ~504 km
        val distance = calculator.calculate(
            lat1 = 52.52,
            lon1 = 13.405,
            lat2 = 48.1351,
            lon2 = 11.5820,
        )
        assertEquals(504.0, distance, 5.0)
    }

    @Test
    fun `calculate returns correct distance between New York and Los Angeles`() = runTest {
        // New York: 40.7128, -74.0060
        // Los Angeles: 34.0522, -118.2437
        // Expected distance: ~3940 km
        val distance = calculator.calculate(
            lat1 = 40.7128,
            lon1 = -74.0060,
            lat2 = 34.0522,
            lon2 = -118.2437,
        )
        assertEquals(3940.0, distance, 20.0)
    }

    @Test
    fun `calculate returns correct short distance`() = runTest {
        // Two points approximately 10km apart in Berlin area
        // Start: 52.52, 13.405
        // End: 52.61, 13.405 (roughly 10km north)
        val distance = calculator.calculate(
            lat1 = 52.52,
            lon1 = 13.405,
            lat2 = 52.61,
            lon2 = 13.405,
        )
        assertEquals(10.0, distance, 1.0)
    }

    @Test
    fun `calculate handles negative coordinates`() = runTest {
        // Sydney: -33.8688, 151.2093
        // Auckland: -36.8509, 174.7645
        // Expected distance: ~2155 km
        val distance = calculator.calculate(
            lat1 = -33.8688,
            lon1 = 151.2093,
            lat2 = -36.8509,
            lon2 = 174.7645,
        )
        assertEquals(2155.0, distance, 20.0)
    }

    @Test
    fun `calculate is symmetric`() = runTest {
        val distance1 = calculator.calculate(
            lat1 = 52.52,
            lon1 = 13.405,
            lat2 = 48.1351,
            lon2 = 11.5820,
        )
        val distance2 = calculator.calculate(
            lat1 = 48.1351,
            lon1 = 11.5820,
            lat2 = 52.52,
            lon2 = 13.405,
        )
        assertEquals(distance1, distance2, 0.001)
    }
}
