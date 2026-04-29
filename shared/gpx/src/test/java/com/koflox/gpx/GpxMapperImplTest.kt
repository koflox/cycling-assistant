package com.koflox.gpx

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxMapperImplTest {

    companion object {
        private const val START_TIME_MS = 1704067200000L // 2024-01-01T00:00:00Z
    }

    private val mapper: GpxMapper = GpxMapperImpl()

    @Test
    fun `maps single segment with all fields`() {
        val input = GpxInput(
            name = "Mount Fuji",
            startTimeMs = START_TIME_MS,
            trackPoints = listOf(
                GpxTrackPoint(
                    latitude = 35.3606,
                    longitude = 138.7274,
                    timestampMs = START_TIME_MS,
                    altitudeMeters = 3776.0,
                    powerWatts = 200,
                    isSegmentStart = true,
                ),
                GpxTrackPoint(
                    latitude = 35.3610,
                    longitude = 138.7280,
                    timestampMs = START_TIME_MS + 5000L,
                    altitudeMeters = 3780.0,
                    powerWatts = 210,
                    isSegmentStart = false,
                ),
            ),
        )

        val gpx = mapper.map(input)

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
        val input = GpxInput(
            name = "Multi-segment",
            startTimeMs = START_TIME_MS,
            trackPoints = listOf(
                trackPoint(35.36, 138.72, START_TIME_MS, isSegmentStart = true),
                trackPoint(35.37, 138.73, START_TIME_MS + 1000L),
                trackPoint(35.38, 138.74, START_TIME_MS + 10000L, isSegmentStart = true),
                trackPoint(35.39, 138.75, START_TIME_MS + 11000L),
            ),
        )

        val gpx = mapper.map(input)

        val segmentCount = "<trkseg>".toRegex().findAll(gpx).count()
        val segmentCloseCount = "</trkseg>".toRegex().findAll(gpx).count()
        assertTrue(segmentCount == 2)
        assertTrue(segmentCloseCount == 2)
    }

    @Test
    fun `omits altitude when null`() {
        val input = GpxInput(
            name = "No altitude",
            startTimeMs = START_TIME_MS,
            trackPoints = listOf(trackPoint(35.36, 138.72, START_TIME_MS, altitudeMeters = null, isSegmentStart = true)),
        )

        val gpx = mapper.map(input)

        assertFalse(gpx.contains("<ele>"))
    }

    @Test
    fun `omits power extension when null`() {
        val input = GpxInput(
            name = "No power",
            startTimeMs = START_TIME_MS,
            trackPoints = listOf(trackPoint(35.36, 138.72, START_TIME_MS, powerWatts = null, isSegmentStart = true)),
        )

        val gpx = mapper.map(input)

        assertFalse(gpx.contains("<extensions>"))
        assertFalse(gpx.contains("<gpxtpx:power>"))
    }

    @Test
    fun `escapes XML special characters in name`() {
        val input = GpxInput(
            name = "Route <A> & \"B\"",
            startTimeMs = START_TIME_MS,
            trackPoints = emptyList(),
        )

        val gpx = mapper.map(input)

        assertTrue(gpx.contains("<name>Route &lt;A&gt; &amp; &quot;B&quot;</name>"))
    }

    @Test
    fun `produces valid GPX header with namespaces`() {
        val input = GpxInput(name = "x", startTimeMs = START_TIME_MS, trackPoints = emptyList())

        val gpx = mapper.map(input)

        assertTrue(gpx.contains("xmlns=\"http://www.topografix.com/GPX/1/1\""))
        assertTrue(gpx.contains("xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\""))
        assertTrue(gpx.contains("version=\"1.1\""))
    }

    @Test
    fun `handles empty track points`() {
        val input = GpxInput(name = "x", startTimeMs = START_TIME_MS, trackPoints = emptyList())

        val gpx = mapper.map(input)

        assertTrue(gpx.contains("<trk>"))
        assertTrue(gpx.contains("</trk>"))
        assertFalse(gpx.contains("<trkpt"))
    }

    @Test
    fun `formats timestamp in ISO 8601 UTC`() {
        val input = GpxInput(
            name = "x",
            startTimeMs = START_TIME_MS,
            trackPoints = listOf(trackPoint(35.36, 138.72, START_TIME_MS, isSegmentStart = true)),
        )

        val gpx = mapper.map(input)

        assertTrue(gpx.contains("<time>2024-01-01T00:00:00Z</time>"))
    }

    private fun trackPoint(
        latitude: Double,
        longitude: Double,
        timestampMs: Long,
        altitudeMeters: Double? = 100.0,
        powerWatts: Int? = null,
        isSegmentStart: Boolean = false,
    ) = GpxTrackPoint(
        latitude = latitude,
        longitude = longitude,
        altitudeMeters = altitudeMeters,
        timestampMs = timestampMs,
        powerWatts = powerWatts,
        isSegmentStart = isSegmentStart,
    )
}
