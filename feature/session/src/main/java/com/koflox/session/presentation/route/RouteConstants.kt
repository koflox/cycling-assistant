package com.koflox.session.presentation.route

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.koflox.graphics.figures.createCircleBitmap

internal object RouteColors {
    val NormalSpeed = Color(0xFF42A5F5)
    val FastSpeed = Color(0xFF7C4DFF)
    val Gap = Color(0xFFBDBDBD)
    val StartMarker = Color(0xFF5A6BD5)
    val EndMarker = Color(0xFFE84940)
}

internal const val ROUTE_WIDTH = 10f
internal const val DASH_LENGTH = 20f
internal const val GAP_LENGTH = 15f
internal const val START_MARKER_SIZE_DP = 14
internal const val MARKER_STROKE_WIDTH_DP = 2
internal val GAP_PATTERN = listOf(Dash(DASH_LENGTH), Gap(GAP_LENGTH))

internal fun createStartMarkerIcon(density: Float): BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
    createCircleBitmap(
        sizeDp = START_MARKER_SIZE_DP,
        strokeWidthDp = MARKER_STROKE_WIDTH_DP,
        fillColor = android.graphics.Color.WHITE,
        strokeColor = RouteColors.StartMarker.toArgb(),
        density = density,
    ),
)
