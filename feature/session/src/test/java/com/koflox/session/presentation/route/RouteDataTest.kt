package com.koflox.session.presentation.route

import androidx.compose.ui.graphics.toArgb
import com.koflox.map.RouteColors
import com.koflox.session.testutil.createTrackPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDataTest {

    companion object {
        private val NORMAL_COLOR = RouteColors.NormalSpeed.toArgb()
        private val FAST_COLOR = RouteColors.FastSpeed.toArgb()
        private val LOW_POWER_COLOR = RouteColors.LowPower.toArgb()
        private val HIGH_POWER_COLOR = RouteColors.HighPower.toArgb()
        private const val NORMAL_SPEED = 20.0
        private const val FAST_SPEED = 40.0
    }

    private val speedStrategy = SpeedRouteColorStrategy()
    private val defaultStrategy = DefaultRouteColorStrategy()
    private val powerStrategy = PowerRouteColorStrategy()

    @Test
    fun `empty track points returns empty`() {
        val result = buildRouteDisplayData(emptyList(), speedStrategy)
        assertEquals(RouteDisplayData.EMPTY, result)
    }

    @Test
    fun `single track point produces no segments`() {
        val points = listOf(createTrackPoint(latitude = 1.0, longitude = 1.0))
        val result = buildRouteDisplayData(points, speedStrategy)
        assertTrue(result.segments.isEmpty())
        assertEquals(1, result.allPoints.size)
    }

    @Test
    fun `two points same speed produces single solid span`() {
        val points = listOf(
            createTrackPoint(latitude = 1.0, longitude = 1.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 2.0, longitude = 2.0, speedKmh = NORMAL_SPEED),
        )
        val result = buildRouteDisplayData(points, speedStrategy)
        assertEquals(1, result.segments.size)
        val colorSpans = result.segments[0].colorSpans
        assertEquals(1, colorSpans.size)
        assertTrue(colorSpans[0] is ColorSpanData.Solid)
        assertEquals(NORMAL_COLOR, (colorSpans[0] as ColorSpanData.Solid).colorArgb)
        assertEquals(1.0, colorSpans[0].length, 0.0)
    }

    @Test
    fun `uniform normal speed across many points produces single solid span`() {
        val points = (0 until 10).map {
            createTrackPoint(latitude = it.toDouble(), longitude = it.toDouble(), speedKmh = NORMAL_SPEED)
        }
        val result = buildRouteDisplayData(points, speedStrategy)
        assertEquals(1, result.segments.size)
        assertEquals(1, result.segments[0].colorSpans.size)
        assertTrue(result.segments[0].colorSpans[0] is ColorSpanData.Solid)
        assertEquals(9.0, result.segments[0].colorSpans[0].length, 0.0)
    }

    @Test
    fun `uniform fast speed produces single solid span with fast color`() {
        val points = (0 until 10).map {
            createTrackPoint(latitude = it.toDouble(), longitude = it.toDouble(), speedKmh = FAST_SPEED)
        }
        val result = buildRouteDisplayData(points, speedStrategy)
        assertEquals(1, result.segments.size)
        val span = result.segments[0].colorSpans[0]
        assertTrue(span is ColorSpanData.Solid)
        assertEquals(FAST_COLOR, (span as ColorSpanData.Solid).colorArgb)
    }

    @Test
    fun `speed transition produces gradient span`() {
        val normalPoints = (0 until 8).map {
            createTrackPoint(latitude = it.toDouble(), longitude = it.toDouble(), speedKmh = NORMAL_SPEED)
        }
        val fastPoints = (8 until 16).map {
            createTrackPoint(latitude = it.toDouble(), longitude = it.toDouble(), speedKmh = FAST_SPEED)
        }
        val result = buildRouteDisplayData(normalPoints + fastPoints, speedStrategy)
        assertEquals(1, result.segments.size)
        val colorSpans = result.segments[0].colorSpans
        assertTrue(colorSpans.any { it is ColorSpanData.Gradient })
        assertTotalLengthEquals(15.0, colorSpans)
    }

    @Test
    fun `rapid speed oscillation does not crash`() {
        val points = listOf(35.0, 0.0, 0.0, 0.0, 119.0).mapIndexed { i, speed ->
            createTrackPoint(latitude = i.toDouble(), longitude = i.toDouble(), speedKmh = speed)
        }
        val result = buildRouteDisplayData(points, speedStrategy)
        assertEquals(1, result.segments.size)
        val colorSpans = result.segments[0].colorSpans
        assertTrue(colorSpans.isNotEmpty())
        assertTotalLengthEquals(4.0, colorSpans)
    }

    @Test
    fun `multiple segments separated by segment start`() {
        val points = listOf(
            createTrackPoint(latitude = 1.0, longitude = 1.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 2.0, longitude = 2.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 3.0, longitude = 3.0, speedKmh = NORMAL_SPEED, isSegmentStart = true),
            createTrackPoint(latitude = 4.0, longitude = 4.0, speedKmh = NORMAL_SPEED),
        )
        val result = buildRouteDisplayData(points, speedStrategy)
        assertEquals(2, result.segments.size)
        assertEquals(1, result.gapPolylines.size)
        assertEquals(4, result.allPoints.size)
    }

    @Test
    fun `gap polyline connects last point of segment to first of next`() {
        val points = listOf(
            createTrackPoint(latitude = 1.0, longitude = 10.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 2.0, longitude = 20.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 3.0, longitude = 30.0, speedKmh = NORMAL_SPEED, isSegmentStart = true),
            createTrackPoint(latitude = 4.0, longitude = 40.0, speedKmh = NORMAL_SPEED),
        )
        val result = buildRouteDisplayData(points, speedStrategy)
        val gap = result.gapPolylines[0]
        assertEquals(2.0, gap[0].latitude, 0.0)
        assertEquals(3.0, gap[1].latitude, 0.0)
    }

    @Test
    fun `single point segment is filtered out`() {
        val points = listOf(
            createTrackPoint(latitude = 1.0, longitude = 1.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 2.0, longitude = 2.0, speedKmh = NORMAL_SPEED, isSegmentStart = true),
            createTrackPoint(latitude = 3.0, longitude = 3.0, speedKmh = NORMAL_SPEED, isSegmentStart = true),
            createTrackPoint(latitude = 4.0, longitude = 4.0, speedKmh = NORMAL_SPEED),
        )
        val result = buildRouteDisplayData(points, speedStrategy)
        assertEquals(1, result.segments.size)
        assertEquals(2, result.segments[0].points.size)
        assertEquals(2, result.gapPolylines.size)
    }

    @Test
    fun `span lengths sum equals edge count`() {
        val normalPoints = (0 until 6).map {
            createTrackPoint(latitude = it.toDouble(), longitude = it.toDouble(), speedKmh = NORMAL_SPEED)
        }
        val fastPoints = (6 until 12).map {
            createTrackPoint(latitude = it.toDouble(), longitude = it.toDouble(), speedKmh = FAST_SPEED)
        }
        val result = buildRouteDisplayData(normalPoints + fastPoints, speedStrategy)
        val colorSpans = result.segments[0].colorSpans
        assertTotalLengthEquals(11.0, colorSpans)
    }

    @Test
    fun `two points different speed produces gradient`() {
        val points = listOf(
            createTrackPoint(latitude = 1.0, longitude = 1.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 2.0, longitude = 2.0, speedKmh = FAST_SPEED),
        )
        val result = buildRouteDisplayData(points, speedStrategy)
        assertEquals(1, result.segments.size)
        val colorSpans = result.segments[0].colorSpans
        assertEquals(1, colorSpans.size)
        assertTotalLengthEquals(1.0, colorSpans)
    }

    @Test
    fun `three segments with two gaps`() {
        val points = listOf(
            createTrackPoint(latitude = 1.0, longitude = 1.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 2.0, longitude = 2.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 3.0, longitude = 3.0, speedKmh = NORMAL_SPEED, isSegmentStart = true),
            createTrackPoint(latitude = 4.0, longitude = 4.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 5.0, longitude = 5.0, speedKmh = NORMAL_SPEED, isSegmentStart = true),
            createTrackPoint(latitude = 6.0, longitude = 6.0, speedKmh = NORMAL_SPEED),
        )
        val result = buildRouteDisplayData(points, speedStrategy)
        assertEquals(3, result.segments.size)
        assertEquals(2, result.gapPolylines.size)
        assertEquals(6, result.allPoints.size)
    }

    @Test
    fun `DefaultRouteColorStrategy produces single color`() {
        val points = listOf(
            createTrackPoint(latitude = 1.0, longitude = 1.0, speedKmh = NORMAL_SPEED),
            createTrackPoint(latitude = 2.0, longitude = 2.0, speedKmh = FAST_SPEED),
            createTrackPoint(latitude = 3.0, longitude = 3.0, speedKmh = NORMAL_SPEED),
        )
        val result = buildRouteDisplayData(points, defaultStrategy)
        assertEquals(1, result.segments.size)
        val colorSpans = result.segments[0].colorSpans
        assertEquals(1, colorSpans.size)
        assertTrue(colorSpans[0] is ColorSpanData.Solid)
        assertEquals(NORMAL_COLOR, (colorSpans[0] as ColorSpanData.Solid).colorArgb)
    }

    @Test
    fun `PowerRouteColorStrategy maps power zones`() {
        val points = (0 until 8).map {
            createTrackPoint(latitude = it.toDouble(), longitude = it.toDouble(), powerWatts = 100)
        } + (8 until 16).map {
            createTrackPoint(latitude = it.toDouble(), longitude = it.toDouble(), powerWatts = 300)
        }
        val result = buildRouteDisplayData(points, powerStrategy)
        assertEquals(1, result.segments.size)
        val colorSpans = result.segments[0].colorSpans
        assertTrue(colorSpans.isNotEmpty())
        val firstSolid = colorSpans.first() as ColorSpanData.Solid
        val lastSolid = colorSpans.last()
        assertEquals(LOW_POWER_COLOR, firstSolid.colorArgb)
        assertEquals(HIGH_POWER_COLOR, lastSolid.endColorArgb)
    }

    private fun assertTotalLengthEquals(expected: Double, colorSpans: List<ColorSpanData>) {
        assertEquals(expected, colorSpans.sumOf { it.length }, 0.001)
    }
}
