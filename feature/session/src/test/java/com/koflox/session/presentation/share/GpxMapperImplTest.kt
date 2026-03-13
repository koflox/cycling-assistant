package com.koflox.session.presentation.share

import com.koflox.session.testutil.createSession
import com.koflox.session.testutil.createTrackPoint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxMapperImplTest {

    companion object {
        private const val SESSION_ID = "session-1"
        private const val DESTINATION_NAME = "Mount Fuji"
        private const val START_TIME_MS = 1704067200000L // 2024-01-01T00:00:00Z
    }

    private val mapper: GpxMapper = GpxMapperImpl()

    @Test
    fun `maps single segment with all fields`() {
        val session = createSession(
            id = SESSION_ID,
            destinationName = DESTINATION_NAME,
            startTimeMs = START_TIME_MS,
            trackPoints = listOf(
                createTrackPoint(
                    latitude = 35.3606,
                    longitude = 138.7274,
                    timestampMs = START_TIME_MS,
                    altitudeMeters = 3776.0,
                    powerWatts = 200,
                    isSegmentStart = true,
                ),
                createTrackPoint(
                    latitude = 35.3610,
                    longitude = 138.7280,
                    timestampMs = START_TIME_MS + 5000L,
                    altitudeMeters = 3780.0,
                    powerWatts = 210,
                ),
            ),
        )

        val gpx = mapper.map(session)

        assertTrue(gpx.contains("<name>Mount Fuji</name>"))
        assertTrue(gpx.contains("<time>2024-01-01T00:00:00Z</time>"))
        assertTrue(gpx.contains("<trkpt lat=\"35.3606\" lon=\"138.7274\">"))
        assertTrue(gpx.contains("<ele>3776.0</ele>"))
        assertTrue(gpx.contains("<gpxtpx:power>200</gpxtpx:power>"))
        assertTrue(gpx.contains("<trkpt lat=\"35.361\" lon=\"138.728\">"))
        assertTrue(gpx.contains("<gpxtpx:power>210</gpxtpx:power>"))
    }

    @Test
    fun `maps multiple segments with trkseg boundaries`() {
        val session = createSession(
            id = SESSION_ID,
            startTimeMs = START_TIME_MS,
            trackPoints = listOf(
                createTrackPoint(latitude = 35.36, longitude = 138.72, isSegmentStart = true, timestampMs = START_TIME_MS),
                createTrackPoint(latitude = 35.37, longitude = 138.73, timestampMs = START_TIME_MS + 1000L),
                createTrackPoint(latitude = 35.38, longitude = 138.74, isSegmentStart = true, timestampMs = START_TIME_MS + 10000L),
                createTrackPoint(latitude = 35.39, longitude = 138.75, timestampMs = START_TIME_MS + 11000L),
            ),
        )

        val gpx = mapper.map(session)

        val segmentCount = "<trkseg>".toRegex().findAll(gpx).count()
        val segmentCloseCount = "</trkseg>".toRegex().findAll(gpx).count()
        assertTrue(segmentCount == 2)
        assertTrue(segmentCloseCount == 2)
    }

    @Test
    fun `omits altitude when null`() {
        val session = createSession(
            id = SESSION_ID,
            startTimeMs = START_TIME_MS,
            trackPoints = listOf(
                createTrackPoint(latitude = 35.36, longitude = 138.72, altitudeMeters = null, isSegmentStart = true, timestampMs = START_TIME_MS),
            ),
        )

        val gpx = mapper.map(session)

        assertFalse(gpx.contains("<ele>"))
    }

    @Test
    fun `omits power extension when null`() {
        val session = createSession(
            id = SESSION_ID,
            startTimeMs = START_TIME_MS,
            trackPoints = listOf(
                createTrackPoint(latitude = 35.36, longitude = 138.72, powerWatts = null, isSegmentStart = true, timestampMs = START_TIME_MS),
            ),
        )

        val gpx = mapper.map(session)

        assertFalse(gpx.contains("<extensions>"))
        assertFalse(gpx.contains("<gpxtpx:power>"))
    }

    @Test
    fun `uses Free Roam when destination name is null`() {
        val session = createSession(
            id = SESSION_ID,
            destinationName = null,
            startTimeMs = START_TIME_MS,
            trackPoints = emptyList(),
        )

        val gpx = mapper.map(session)

        assertTrue(gpx.contains("<name>Free Roam</name>"))
    }

    @Test
    fun `escapes XML special characters in destination name`() {
        val session = createSession(
            id = SESSION_ID,
            destinationName = "Route <A> & \"B\"",
            startTimeMs = START_TIME_MS,
            trackPoints = emptyList(),
        )

        val gpx = mapper.map(session)

        assertTrue(gpx.contains("<name>Route &lt;A&gt; &amp; &quot;B&quot;</name>"))
    }

    @Test
    fun `produces valid GPX header with namespaces`() {
        val session = createSession(
            id = SESSION_ID,
            startTimeMs = START_TIME_MS,
            trackPoints = emptyList(),
        )

        val gpx = mapper.map(session)

        assertTrue(gpx.contains("xmlns=\"http://www.topografix.com/GPX/1/1\""))
        assertTrue(gpx.contains("xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\""))
        assertTrue(gpx.contains("version=\"1.1\""))
    }

    @Test
    fun `handles empty track points`() {
        val session = createSession(
            id = SESSION_ID,
            startTimeMs = START_TIME_MS,
            trackPoints = emptyList(),
        )

        val gpx = mapper.map(session)

        assertTrue(gpx.contains("<trk>"))
        assertTrue(gpx.contains("</trk>"))
        assertFalse(gpx.contains("<trkpt"))
    }

    @Test
    fun `formats timestamp in ISO 8601 UTC`() {
        val session = createSession(
            id = SESSION_ID,
            startTimeMs = 1704067200000L, // 2024-01-01T00:00:00Z
            trackPoints = listOf(
                createTrackPoint(
                    latitude = 35.36,
                    longitude = 138.72,
                    timestampMs = 1704067200000L,
                    isSegmentStart = true,
                ),
            ),
        )

        val gpx = mapper.map(session)

        assertTrue(gpx.contains("<time>2024-01-01T00:00:00Z</time>"))
    }
}
