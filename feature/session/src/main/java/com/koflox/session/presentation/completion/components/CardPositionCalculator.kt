package com.koflox.session.presentation.completion.components

import androidx.compose.ui.Alignment
import com.google.android.gms.maps.model.LatLng

/**
 * Calculates the best position for the stats card to avoid overlapping with route markers.
 *
 * Positions are checked in priority order:
 * 1. Bottom corners (BottomStart, BottomEnd)
 * 2. Bottom center (BottomCenter)
 * 3. Top corners (TopStart, TopEnd)
 * 4. Top center (TopCenter)
 * 5. Side centers (CenterStart, CenterEnd)
 */
internal fun calculateCardAlignment(routePoints: List<LatLng>): Alignment {
    if (routePoints.size < 2) return Alignment.BottomCenter

    val startPoint = routePoints.first()
    val endPoint = routePoints.last()
    val (startRegion, endRegion) = calculateMarkerRegions(routePoints, startPoint, endPoint)

    val prioritizedPositions = listOf(
        Alignment.BottomStart to ScreenRegion.BOTTOM_START,
        Alignment.BottomEnd to ScreenRegion.BOTTOM_END,
        Alignment.BottomCenter to ScreenRegion.BOTTOM_CENTER,
        Alignment.TopStart to ScreenRegion.TOP_START,
        Alignment.TopEnd to ScreenRegion.TOP_END,
        Alignment.TopCenter to ScreenRegion.TOP_CENTER,
        Alignment.CenterStart to ScreenRegion.CENTER_START,
        Alignment.CenterEnd to ScreenRegion.CENTER_END,
    )

    return prioritizedPositions
        .firstOrNull { (_, region) -> region != startRegion && region != endRegion }
        ?.first
        ?: Alignment.BottomCenter
}

private fun calculateMarkerRegions(
    routePoints: List<LatLng>,
    startPoint: LatLng,
    endPoint: LatLng,
): Pair<ScreenRegion, ScreenRegion> {
    val bounds = calculateBounds(routePoints)
    val startNormalized = normalizePoint(startPoint, bounds)
    val endNormalized = normalizePoint(endPoint, bounds)

    return Pair(
        getScreenRegion(startNormalized),
        getScreenRegion(endNormalized),
    )
}

private data class LatLngBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double,
)

private data class NormalizedPoint(val x: Double, val y: Double)

private fun calculateBounds(points: List<LatLng>): LatLngBounds {
    var minLat = Double.MAX_VALUE
    var maxLat = Double.MIN_VALUE
    var minLng = Double.MAX_VALUE
    var maxLng = Double.MIN_VALUE

    for (point in points) {
        minLat = minOf(minLat, point.latitude)
        maxLat = maxOf(maxLat, point.latitude)
        minLng = minOf(minLng, point.longitude)
        maxLng = maxOf(maxLng, point.longitude)
    }

    return LatLngBounds(minLat, maxLat, minLng, maxLng)
}

private fun normalizePoint(point: LatLng, bounds: LatLngBounds): NormalizedPoint {
    val latRange = bounds.maxLat - bounds.minLat
    val lngRange = bounds.maxLng - bounds.minLng

    val x = if (lngRange > 0) (point.longitude - bounds.minLng) / lngRange else 0.5
    // Invert Y because latitude increases upward but screen Y increases downward
    val y = if (latRange > 0) 1.0 - (point.latitude - bounds.minLat) / latRange else 0.5

    return NormalizedPoint(x, y)
}

private fun getScreenRegion(point: NormalizedPoint): ScreenRegion {
    val horizontalZone = when {
        point.x < 0.33 -> HorizontalZone.START
        point.x > 0.67 -> HorizontalZone.END
        else -> HorizontalZone.CENTER
    }
    val verticalZone = when {
        point.y < 0.33 -> VerticalZone.TOP
        point.y > 0.67 -> VerticalZone.BOTTOM
        else -> VerticalZone.CENTER
    }

    return when (verticalZone) {
        VerticalZone.TOP -> when (horizontalZone) {
            HorizontalZone.START -> ScreenRegion.TOP_START
            HorizontalZone.CENTER -> ScreenRegion.TOP_CENTER
            HorizontalZone.END -> ScreenRegion.TOP_END
        }

        VerticalZone.CENTER -> when (horizontalZone) {
            HorizontalZone.START -> ScreenRegion.CENTER_START
            HorizontalZone.CENTER -> ScreenRegion.CENTER
            HorizontalZone.END -> ScreenRegion.CENTER_END
        }

        VerticalZone.BOTTOM -> when (horizontalZone) {
            HorizontalZone.START -> ScreenRegion.BOTTOM_START
            HorizontalZone.CENTER -> ScreenRegion.BOTTOM_CENTER
            HorizontalZone.END -> ScreenRegion.BOTTOM_END
        }
    }
}

private enum class HorizontalZone { START, CENTER, END }
private enum class VerticalZone { TOP, CENTER, BOTTOM }

private enum class ScreenRegion {
    TOP_START,
    TOP_CENTER,
    TOP_END,
    CENTER_START,
    CENTER,
    CENTER_END,
    BOTTOM_START,
    BOTTOM_CENTER,
    BOTTOM_END,
}
