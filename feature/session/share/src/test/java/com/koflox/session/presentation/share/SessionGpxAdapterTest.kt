package com.koflox.session.presentation.share

import com.koflox.gpx.GpxTrackPoint
import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createTrackPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionGpxAdapterTest {

    companion object {
        private const val START_TIME_MS = 1704067200000L
    }

    @Test
    fun `toGpxInput uses destinationName when present`() {
        val session = createSession(destinationName = "Mount Fuji", startTimeMs = START_TIME_MS)

        val input = session.toGpxInput()

        assertEquals("Mount Fuji", input.name)
        assertEquals(START_TIME_MS, input.startTimeMs)
    }

    @Test
    fun `toGpxInput falls back to Free Roam when destinationName is null`() {
        val session = createSession(destinationName = null, startTimeMs = START_TIME_MS)

        val input = session.toGpxInput()

        assertEquals("Free Roam", input.name)
    }

    @Test
    fun `toGpxInput maps every TrackPoint field`() {
        val session = createSession(
            destinationName = "x",
            startTimeMs = START_TIME_MS,
            trackPoints = listOf(
                createTrackPoint(
                    latitude = 35.36,
                    longitude = 138.72,
                    timestampMs = START_TIME_MS,
                    altitudeMeters = 100.0,
                    powerWatts = 200,
                    isSegmentStart = true,
                ),
            ),
        )

        val input = session.toGpxInput()

        assertEquals(1, input.trackPoints.size)
        assertEquals(
            GpxTrackPoint(
                latitude = 35.36,
                longitude = 138.72,
                altitudeMeters = 100.0,
                timestampMs = START_TIME_MS,
                powerWatts = 200,
                isSegmentStart = true,
            ),
            input.trackPoints[0],
        )
    }

    @Test
    fun `toGpxInput maps empty trackPoints to empty list`() {
        val session = createSession(destinationName = "x", trackPoints = emptyList())

        val input = session.toGpxInput()

        assertEquals(emptyList<GpxTrackPoint>(), input.trackPoints)
    }

    @Test
    fun `toGpxInput preserves null altitude and power`() {
        val session = createSession(
            destinationName = "x",
            trackPoints = listOf(
                createTrackPoint(latitude = 35.36, longitude = 138.72, altitudeMeters = null, powerWatts = null),
            ),
        )

        val input = session.toGpxInput()

        assertEquals(null, input.trackPoints[0].altitudeMeters)
        assertEquals(null, input.trackPoints[0].powerWatts)
    }
}
