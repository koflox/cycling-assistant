package com.koflox.session.presentation.mapper

import com.koflox.session.testutil.createSession
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionUiMapperImplTest {

    companion object {
        private const val ONE_HOUR_MS = 3600000L
        private const val ONE_MINUTE_MS = 60000L
        private const val ONE_SECOND_MS = 1000L
        private const val START_TIME_MS = 1700000000000L
    }

    private lateinit var mapper: SessionUiMapperImpl

    @Before
    fun setup() {
        mapper = SessionUiMapperImpl()
    }

    @Test
    fun `formatElapsedTime formats zero`() {
        val result = mapper.formatElapsedTime(0L)

        assertEquals("00:00:00", result)
    }

    @Test
    fun `formatElapsedTime formats hours minutes seconds`() {
        val result = mapper.formatElapsedTime(2 * ONE_HOUR_MS + 15 * ONE_MINUTE_MS + 30 * ONE_SECOND_MS)

        assertEquals("02:15:30", result)
    }

    @Test
    fun `formatElapsedTime formats large values`() {
        val result = mapper.formatElapsedTime(100 * ONE_HOUR_MS)

        assertEquals("100:00:00", result)
    }

    @Test
    fun `formatDistance formats with two decimals`() {
        val result = mapper.formatDistance(12.345)

        assertEquals("12.35", result)
    }

    @Test
    fun `formatDistance formats zero`() {
        val result = mapper.formatDistance(0.0)

        assertEquals("0.00", result)
    }

    @Test
    fun `formatSpeed formats with one decimal`() {
        val result = mapper.formatSpeed(25.67)

        assertEquals("25.7", result)
    }

    @Test
    fun `formatAltitudeGain formats as whole number`() {
        val result = mapper.formatAltitudeGain(150.7)

        assertEquals("151", result)
    }

    @Test
    fun `formatCalories formats as whole number`() {
        val result = mapper.formatCalories(450.3)

        assertEquals("450", result)
    }

    @Test
    fun `formatStartDate formats correctly`() {
        val expected = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(START_TIME_MS))

        val result = mapper.formatStartDate(START_TIME_MS)

        assertEquals(expected, result)
    }

    @Test
    fun `toSessionUiModel maps all fields`() {
        val session = createSession(
            elapsedTimeMs = ONE_HOUR_MS,
            traveledDistanceKm = 10.5,
            averageSpeedKmh = 21.0,
            topSpeedKmh = 35.5,
            totalAltitudeGainMeters = 200.0,
        )

        val result = mapper.toSessionUiModel(session)

        assertEquals("01:00:00", result.elapsedTimeFormatted)
        assertEquals("10.50", result.traveledDistanceFormatted)
        assertEquals("21.0", result.averageSpeedFormatted)
        assertEquals("35.5", result.topSpeedFormatted)
        assertEquals("200", result.altitudeGainFormatted)
    }
}
