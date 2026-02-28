package com.koflox.session.presentation.route

import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.StrokeStyle
import com.google.android.gms.maps.model.StyleSpan
import com.koflox.map.RouteColors
import com.koflox.session.domain.model.TrackPoint

internal data class RoutePoint(
    val latLng: LatLng,
    val speedKmh: Double,
)

internal data class RouteSegment(
    val points: List<RoutePoint>,
)

sealed interface ColorSpanData {
    val length: Double
    val endColorArgb: Int

    data class Solid(
        val colorArgb: Int,
        override val length: Double,
    ) : ColorSpanData {
        override val endColorArgb: Int get() = colorArgb
    }

    data class Gradient(
        val fromColorArgb: Int,
        val toColorArgb: Int,
        override val length: Double,
    ) : ColorSpanData {
        override val endColorArgb: Int get() = toColorArgb
    }
}

data class SegmentDisplayData(
    val points: List<LatLng>,
    val spans: List<StyleSpan>,
    val colorSpans: List<ColorSpanData> = emptyList(),
)

private const val SPEED_THRESHOLD_FAST = 30.0
private val SPEED_COLOR_NORMAL_ARGB = RouteColors.NormalSpeed.toArgb()
private val SPEED_COLOR_FAST_ARGB = RouteColors.FastSpeed.toArgb()
private const val SPEED_SMOOTHING_WINDOW = 5
private const val GRADIENT_HALF_WIDTH = 3

data class RouteDisplayData(
    val segments: List<SegmentDisplayData>,
    val gapPolylines: List<List<LatLng>>,
    val allPoints: List<LatLng>,
) {

    companion object {
        val EMPTY = RouteDisplayData(emptyList(), emptyList(), emptyList())
    }
}

internal fun buildRouteDisplayData(trackPoints: List<TrackPoint>): RouteDisplayData {
    if (trackPoints.isEmpty()) return RouteDisplayData.EMPTY
    val rawSegments = buildRawSegments(trackPoints)
    val gapPolylines = (0 until rawSegments.size - 1).map { i ->
        listOf(rawSegments[i].last().latLng, rawSegments[i + 1].first().latLng)
    }
    val routeSegments = rawSegments.filter { it.size >= 2 }.map(::RouteSegment)
    val segments = routeSegments.map { segment ->
        val pointColors = smoothSpeedColors(segment.points)
        val (spans, colorSpans) = buildSpans(pointColors)
        SegmentDisplayData(
            points = segment.points.map { it.latLng },
            spans = spans,
            colorSpans = colorSpans,
        )
    }
    val allPoints = rawSegments.flatMap { s -> s.map(RoutePoint::latLng) }
    return RouteDisplayData(segments, gapPolylines, allPoints)
}

private fun buildRawSegments(trackPoints: List<TrackPoint>): List<List<RoutePoint>> {
    val segments = mutableListOf<MutableList<RoutePoint>>()
    trackPoints.forEach { tp ->
        if (tp.isSegmentStart || segments.isEmpty()) {
            segments.add(mutableListOf())
        }
        segments.last().add(RoutePoint(LatLng(tp.latitude, tp.longitude), tp.speedKmh))
    }
    return segments
}

private fun smoothSpeedColors(points: List<RoutePoint>): List<Int> {
    val halfWindow = SPEED_SMOOTHING_WINDOW / 2
    return List(points.size) { i ->
        val start = maxOf(0, i - halfWindow)
        val end = minOf(points.size, i + halfWindow + 1)
        var sum = 0.0
        for (j in start until end) {
            sum += points[j].speedKmh
        }
        speedToColor(sum / (end - start))
    }
}

private fun speedToColor(speedKmh: Double): Int = if (speedKmh < SPEED_THRESHOLD_FAST) SPEED_COLOR_NORMAL_ARGB else SPEED_COLOR_FAST_ARGB

private fun buildSpans(pointColors: List<Int>): Pair<List<StyleSpan>, List<ColorSpanData>> {
    val edgeCount = pointColors.size - 1
    if (edgeCount < 1) return emptyList<StyleSpan>() to emptyList()
    val transitions = (0 until edgeCount).filter { pointColors[it] != pointColors[it + 1] }
    val spans = mutableListOf<StyleSpan>()
    val colorSpans = mutableListOf<ColorSpanData>()
    var pos = 0
    for (t in transitions) {
        val gradStart = maxOf(pos, t - GRADIENT_HALF_WIDTH)
        val gradEnd = minOf(edgeCount - 1, t + GRADIENT_HALF_WIDTH)
        if (gradEnd < gradStart) continue
        val solidBefore = gradStart - pos
        if (solidBefore > 0) {
            spans.add(StyleSpan(StrokeStyle.colorBuilder(pointColors[pos]).build(), solidBefore.toDouble()))
            colorSpans.add(ColorSpanData.Solid(pointColors[pos], solidBefore.toDouble()))
        }
        val gradWidth = gradEnd - gradStart + 1
        spans.add(StyleSpan(StrokeStyle.gradientBuilder(pointColors[t], pointColors[t + 1]).build(), gradWidth.toDouble()))
        colorSpans.add(ColorSpanData.Gradient(pointColors[t], pointColors[t + 1], gradWidth.toDouble()))
        pos = gradEnd + 1
    }
    val remaining = edgeCount - pos
    if (remaining > 0) {
        spans.add(StyleSpan(StrokeStyle.colorBuilder(pointColors[pos]).build(), remaining.toDouble()))
        colorSpans.add(ColorSpanData.Solid(pointColors[pos], remaining.toDouble()))
    }
    if (spans.isEmpty()) {
        spans.add(StyleSpan(StrokeStyle.colorBuilder(pointColors[0]).build(), edgeCount.toDouble()))
        colorSpans.add(ColorSpanData.Solid(pointColors[0], edgeCount.toDouble()))
    }
    return spans to colorSpans
}
