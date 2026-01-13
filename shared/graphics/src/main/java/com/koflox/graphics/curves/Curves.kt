package com.koflox.graphics.curves

import com.koflox.graphics.primitives.Point

private const val DEFAULT_CURVE_SEGMENTS = 50
private const val DEFAULT_CURVE_HEIGHT_FACTOR = 0.2

fun createCurvePoints(
    start: Point,
    end: Point,
    segments: Int = DEFAULT_CURVE_SEGMENTS,
    heightFactor: Double = DEFAULT_CURVE_HEIGHT_FACTOR,
): List<Point> {
    val is1stQuarter = start.x < end.x && start.y < end.y
    val is2ndQuarter = start.x > end.x && start.y < end.y
    val adjustedHeightFactor = if (is1stQuarter || is2ndQuarter) -heightFactor else heightFactor
    val midX = (start.x + end.x) / 2
    val midY = (start.y + end.y) / 2
    val deltaX = end.x - start.x
    val deltaY = end.y - start.y
    val controlX = midX - deltaY * adjustedHeightFactor
    val controlY = midY + deltaX * adjustedHeightFactor
    return (0..segments).map { i ->
        val t = i.toDouble() / segments
        val oneMinusT = 1 - t
        val x = oneMinusT * oneMinusT * start.x +
            2 * oneMinusT * t * controlX +
            t * t * end.x
        val y = oneMinusT * oneMinusT * start.y +
            2 * oneMinusT * t * controlY +
            t * t * end.y
        Point(x, y)
    }
}
