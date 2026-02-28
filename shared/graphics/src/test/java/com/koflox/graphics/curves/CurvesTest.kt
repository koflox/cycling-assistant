package com.koflox.graphics.curves

import com.koflox.graphics.primitives.Point
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurvesTest {

    companion object {
        private const val DELTA = 0.001
    }

    @Test
    fun `first point equals start`() {
        val start = Point(0.0, 0.0)
        val end = Point(100.0, 50.0)
        val points = createCurvePoints(start, end)
        assertEquals(start.x, points.first().x, DELTA)
        assertEquals(start.y, points.first().y, DELTA)
    }

    @Test
    fun `last point equals end`() {
        val start = Point(0.0, 0.0)
        val end = Point(100.0, 50.0)
        val points = createCurvePoints(start, end)
        assertEquals(end.x, points.last().x, DELTA)
        assertEquals(end.y, points.last().y, DELTA)
    }

    @Test
    fun `point count equals segments plus one`() {
        val segments = 20
        val points = createCurvePoints(Point(0.0, 0.0), Point(100.0, 100.0), segments = segments)
        assertEquals(segments + 1, points.size)
    }

    @Test
    fun `single segment produces two points`() {
        val points = createCurvePoints(Point(0.0, 0.0), Point(10.0, 10.0), segments = 1)
        assertEquals(2, points.size)
    }

    @Test
    fun `identical start and end produces all identical points`() {
        val point = Point(50.0, 50.0)
        val points = createCurvePoints(point, point, segments = 10)
        points.forEach {
            assertEquals(point.x, it.x, DELTA)
            assertEquals(point.y, it.y, DELTA)
        }
    }

    @Test
    fun `curve bows away from straight line`() {
        val start = Point(0.0, 0.0)
        val end = Point(100.0, 0.0)
        val curvePoints = createCurvePoints(start, end, segments = 10, heightFactor = 0.3)
        val midPoint = curvePoints[5]
        assertTrue(midPoint.y != 0.0)
    }

    @Test
    fun `first quarter curve bows away from straight line`() {
        val start = Point(0.0, 0.0)
        val end = Point(100.0, 100.0)
        val points = createCurvePoints(start, end, segments = 10, heightFactor = 0.2)
        val mid = points[5]
        // straight line: y = x, so at midpoint y should differ from x
        assertTrue(mid.y != mid.x)
    }

    @Test
    fun `zero height factor produces straight line`() {
        val start = Point(0.0, 0.0)
        val end = Point(100.0, 100.0)
        val points = createCurvePoints(start, end, segments = 10, heightFactor = 0.0)
        points.forEachIndexed { i, point ->
            val t = i.toDouble() / 10
            assertEquals(100.0 * t, point.x, DELTA)
            assertEquals(100.0 * t, point.y, DELTA)
        }
    }
}
