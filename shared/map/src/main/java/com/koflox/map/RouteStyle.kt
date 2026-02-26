package com.koflox.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.koflox.graphics.figures.createArrowBitmap
import com.koflox.graphics.figures.createCircleBitmap

object RouteColors {
    val NormalSpeed = Color(0xFF42A5F5)
    val FastSpeed = Color(0xFF7C4DFF)
    val Gap = Color(0xFFBDBDBD)
    val StartMarker = Color(0xFF5A6BD5)
    val EndMarker = Color(0xFFE84940)
}

const val ROUTE_WIDTH = 10f
const val ROUTE_DASH_LENGTH = 20f
const val ROUTE_GAP_LENGTH = 15f
const val ROUTE_START_MARKER_SIZE_DP = 14
const val ROUTE_END_MARKER_SIZE_DP = 20
const val ROUTE_MARKER_STROKE_WIDTH_DP = 2
val ROUTE_GAP_PATTERN = listOf(Dash(ROUTE_DASH_LENGTH), Gap(ROUTE_GAP_LENGTH))

fun createStartMarkerIcon(density: Float): BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
    createCircleBitmap(
        sizeDp = ROUTE_START_MARKER_SIZE_DP,
        strokeWidthDp = ROUTE_MARKER_STROKE_WIDTH_DP,
        fillColor = android.graphics.Color.WHITE,
        strokeColor = RouteColors.StartMarker.toArgb(),
        density = density,
    ),
)

fun createEndMarkerIcon(density: Float, rotationDegrees: Float): BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
    createArrowBitmap(
        sizeDp = ROUTE_END_MARKER_SIZE_DP,
        strokeWidthDp = ROUTE_MARKER_STROKE_WIDTH_DP,
        fillColor = android.graphics.Color.WHITE,
        strokeColor = RouteColors.EndMarker.toArgb(),
        density = density,
        rotationDegrees = rotationDegrees,
    ),
)
