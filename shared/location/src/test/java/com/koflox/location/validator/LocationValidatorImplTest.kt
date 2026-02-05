package com.koflox.location.validator

import com.koflox.location.model.Location
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationValidatorImplTest {

    companion object {
        private const val DEFAULT_LAT = 52.52
        private const val DEFAULT_LON = 13.405
        private const val MAX_ACCURACY = 20f
    }

    private lateinit var validator: LocationValidatorImpl

    @Before
    fun setup() {
        validator = LocationValidatorImpl()
    }

    @Test
    fun `returns true when accuracy is null`() {
        val location = createLocation(accuracyMeters = null)

        assertTrue(validator.isAccuracyValid(location))
    }

    @Test
    fun `returns true when accuracy equals max threshold`() {
        val location = createLocation(accuracyMeters = MAX_ACCURACY)

        assertTrue(validator.isAccuracyValid(location))
    }

    @Test
    fun `returns true when accuracy is below max threshold`() {
        val location = createLocation(accuracyMeters = 10f)

        assertTrue(validator.isAccuracyValid(location))
    }

    @Test
    fun `returns false when accuracy exceeds max threshold`() {
        val location = createLocation(accuracyMeters = 20.1f)

        assertFalse(validator.isAccuracyValid(location))
    }

    @Test
    fun `returns true when accuracy is zero`() {
        val location = createLocation(accuracyMeters = 0f)

        assertTrue(validator.isAccuracyValid(location))
    }

    private fun createLocation(accuracyMeters: Float?) = Location(
        latitude = DEFAULT_LAT,
        longitude = DEFAULT_LON,
        accuracyMeters = accuracyMeters,
    )
}
