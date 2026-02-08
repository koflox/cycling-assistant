package com.koflox.location.smoother

import com.koflox.location.model.Location
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class KalmanLocationSmootherTest {

    companion object {
        private const val BASE_LAT = 52.52
        private const val BASE_LON = 13.405
        private const val BASE_ACCURACY = 10.0f
        private const val BASE_TIMESTAMP_MS = 1_000_000L
        private const val INTERVAL_MS = 3_000L
        private const val COORDINATE_DELTA = 0.001
        private const val BASE_ALTITUDE = 50.0
    }

    private lateinit var smoother: KalmanLocationSmoother

    @Before
    fun setup() {
        smoother = KalmanLocationSmoother()
    }

    @Test
    fun `smooth returns first location unchanged`() {
        val location = createLocation()

        val result = smoother.smooth(location, BASE_TIMESTAMP_MS)

        assertEquals(BASE_LAT, result.latitude, 0.0)
        assertEquals(BASE_LON, result.longitude, 0.0)
    }

    @Test
    fun `smooth pulls second location toward first`() {
        val firstLocation = createLocation()
        smoother.smooth(firstLocation, BASE_TIMESTAMP_MS)

        val shiftedLat = BASE_LAT + COORDINATE_DELTA
        val shiftedLon = BASE_LON + COORDINATE_DELTA
        val secondLocation = createLocation(latitude = shiftedLat, longitude = shiftedLon)
        val result = smoother.smooth(secondLocation, BASE_TIMESTAMP_MS + INTERVAL_MS)

        assertTrue(abs(result.latitude - BASE_LAT) < abs(shiftedLat - BASE_LAT))
        assertTrue(abs(result.longitude - BASE_LON) < abs(shiftedLon - BASE_LON))
    }

    @Test
    fun `smooth trusts high accuracy measurement more`() {
        val firstLocation = createLocation(accuracyMeters = BASE_ACCURACY)
        smoother.smooth(firstLocation, BASE_TIMESTAMP_MS)

        val shiftedLat = BASE_LAT + COORDINATE_DELTA
        val highAccuracyLocation = createLocation(latitude = shiftedLat, accuracyMeters = 2.0f)
        val result = smoother.smooth(highAccuracyLocation, BASE_TIMESTAMP_MS + INTERVAL_MS)

        val distanceToMeasurement = abs(result.latitude - shiftedLat)
        val distanceToEstimate = abs(result.latitude - BASE_LAT)
        assertTrue(distanceToMeasurement < distanceToEstimate)
    }

    @Test
    fun `smooth trusts low accuracy measurement less`() {
        val firstLocation = createLocation(accuracyMeters = 5.0f)
        smoother.smooth(firstLocation, BASE_TIMESTAMP_MS)

        val shiftedLat = BASE_LAT + COORDINATE_DELTA
        val lowAccuracyLocation = createLocation(latitude = shiftedLat, accuracyMeters = 50.0f)
        val result = smoother.smooth(lowAccuracyLocation, BASE_TIMESTAMP_MS + INTERVAL_MS)

        val distanceToMeasurement = abs(result.latitude - shiftedLat)
        val distanceToEstimate = abs(result.latitude - BASE_LAT)
        assertTrue(distanceToMeasurement > distanceToEstimate)
    }

    @Test
    fun `smooth returns location unchanged when accuracy is null`() {
        val firstLocation = createLocation()
        smoother.smooth(firstLocation, BASE_TIMESTAMP_MS)

        val shiftedLat = BASE_LAT + COORDINATE_DELTA
        val nullAccuracyLocation = createLocation(latitude = shiftedLat, accuracyMeters = null)
        val result = smoother.smooth(nullAccuracyLocation, BASE_TIMESTAMP_MS + INTERVAL_MS)

        assertEquals(shiftedLat, result.latitude, 0.0)
    }

    @Test
    fun `reset clears state so next point is treated as first`() {
        val firstLocation = createLocation()
        smoother.smooth(firstLocation, BASE_TIMESTAMP_MS)
        smoother.smooth(
            createLocation(latitude = BASE_LAT + COORDINATE_DELTA),
            BASE_TIMESTAMP_MS + INTERVAL_MS,
        )

        smoother.reset()

        val newLat = BASE_LAT + 0.01
        val newLocation = createLocation(latitude = newLat)
        val result = smoother.smooth(newLocation, BASE_TIMESTAMP_MS + INTERVAL_MS * 2)

        assertEquals(newLat, result.latitude, 0.0)
        assertEquals(BASE_LON, result.longitude, 0.0)
    }

    @Test
    fun `smooth with identical points converges to that position`() {
        val location = createLocation()
        smoother.smooth(location, BASE_TIMESTAMP_MS)

        var result = location
        for (i in 1..10) {
            result = smoother.smooth(location, BASE_TIMESTAMP_MS + INTERVAL_MS * i)
        }

        assertEquals(BASE_LAT, result.latitude, 1e-10)
        assertEquals(BASE_LON, result.longitude, 1e-10)
    }

    @Test
    fun `smooth preserves altitude and accuracy from original location`() {
        val firstLocation = createLocation(altitudeMeters = BASE_ALTITUDE)
        smoother.smooth(firstLocation, BASE_TIMESTAMP_MS)

        val secondLocation = createLocation(
            latitude = BASE_LAT + COORDINATE_DELTA,
            altitudeMeters = BASE_ALTITUDE + 10.0,
            accuracyMeters = 15.0f,
        )
        val result = smoother.smooth(secondLocation, BASE_TIMESTAMP_MS + INTERVAL_MS)

        assertEquals(BASE_ALTITUDE + 10.0, result.altitudeMeters!!, 0.0)
        assertEquals(15.0f, result.accuracyMeters!!, 0.0f)
    }

    @Test
    fun `smooth dampens GPS jump`() {
        smoother.smooth(createLocation(), BASE_TIMESTAMP_MS)
        for (i in 1..5) {
            smoother.smooth(createLocation(), BASE_TIMESTAMP_MS + INTERVAL_MS * i)
        }

        val jumpLat = BASE_LAT + 0.01
        val jumpLocation = createLocation(latitude = jumpLat, accuracyMeters = 20.0f)
        val result = smoother.smooth(jumpLocation, BASE_TIMESTAMP_MS + INTERVAL_MS * 6)

        val distanceToJump = abs(result.latitude - jumpLat)
        val distanceToBase = abs(result.latitude - BASE_LAT)
        assertTrue(distanceToBase < distanceToJump)
    }

    private fun createLocation(
        latitude: Double = BASE_LAT,
        longitude: Double = BASE_LON,
        accuracyMeters: Float? = BASE_ACCURACY,
        altitudeMeters: Double? = null,
    ) = Location(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        altitudeMeters = altitudeMeters,
    )
}
