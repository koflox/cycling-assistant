@file:Suppress("ComposeUnstableReceiver")

package com.koflox.session.presentation.completion.components

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withRotation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.koflox.designsystem.theme.LocalDarkTheme
import kotlin.math.atan2
import com.koflox.designsystem.R as DesignSystemR

private const val MAP_PADDING = 100
private val ROUTE_COLOR = Color(0xFF2196F3)
private const val ROUTE_WIDTH = 8f
private val START_MARKER_COLOR = Color(0xFF5A6BD5)
private val END_MARKER_COLOR = Color(0xFFE84940)

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
internal fun RouteMapView(
    routePoints: List<LatLng>,
    modifier: Modifier = Modifier,
    onMapLoaded: (() -> Unit)? = null,
) {
    val isDarkTheme = LocalDarkTheme.current
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val markerSizePx = with(LocalDensity.current) { 24.dp.toPx().toInt() }
    val startMarkerIcon = remember(routePoints, markerSizePx) {
        createStartMarkerIcon(routePoints, markerSizePx)
    }
    val endMarkerIcon = remember(routePoints, markerSizePx) {
        createEndMarkerIcon(routePoints, markerSizePx)
    }
    LaunchedEffect(routePoints) {
        animateCameraToRoute(routePoints, cameraPositionState)
    }
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
        )
    }
    val mapProperties = remember(isDarkTheme) {
        MapProperties(
            mapStyleOptions = if (isDarkTheme) {
                MapStyleOptions.loadRawResourceStyle(context, DesignSystemR.raw.map_style_dark)
            } else {
                null
            },
        )
    }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = mapProperties,
        onMapLoaded = onMapLoaded,
    ) {
        if (routePoints.isNotEmpty()) {
            Marker(
                state = rememberUpdatedMarkerState(position = routePoints.first()),
                title = "Start",
                icon = startMarkerIcon,
                anchor = Offset(0.5f, 0.5f),
            )
        }
        if (routePoints.size >= 2) {
            Polyline(points = routePoints, color = ROUTE_COLOR, width = ROUTE_WIDTH)
            Marker(
                state = rememberUpdatedMarkerState(position = routePoints.last()),
                title = "End",
                icon = endMarkerIcon,
                anchor = Offset(0.5f, 0.5f),
            )
        }
    }
}

private fun createStartMarkerIcon(routePoints: List<LatLng>, sizePx: Int): BitmapDescriptor {
    val rotation = if (routePoints.size >= 2) {
        calculateRotation(routePoints[0], routePoints[1])
    } else {
        0f
    }
    return createArrowBitmap(sizePx, START_MARKER_COLOR, rotation)
}

private fun createEndMarkerIcon(routePoints: List<LatLng>, sizePx: Int): BitmapDescriptor? {
    if (routePoints.size < 2) return null
    val lastIndex = routePoints.lastIndex
    val rotation = calculateRotation(routePoints[lastIndex - 1], routePoints[lastIndex])
    return createArrowBitmap(sizePx, END_MARKER_COLOR, rotation)
}

private fun animateCameraToRoute(
    routePoints: List<LatLng>,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
) {
    when {
        routePoints.size >= 2 -> {
            val bounds = LatLngBounds.Builder().apply {
                routePoints.forEach { include(it) }
            }.build()
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING))
        }

        routePoints.size == 1 -> {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(routePoints.first(), 15f))
        }
    }
}

private fun createArrowBitmap(
    sizePx: Int,
    color: Color,
    rotationDegrees: Float,
): BitmapDescriptor {
    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        this.color = color.toArgb()
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.withRotation(rotationDegrees, sizePx / 2f, sizePx / 2f) {
        val path = Path().apply {
            val centerX = sizePx / 2f
            val centerY = sizePx / 2f
            val arrowWidth = sizePx * 0.6f
            val arrowHeight = sizePx * 0.8f

            moveTo(centerX + arrowHeight / 2, centerY)
            lineTo(centerX - arrowHeight / 2, centerY - arrowWidth / 2)
            lineTo(centerX - arrowHeight / 2, centerY + arrowWidth / 2)
            close()
        }
        drawPath(path, paint)
    }
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun calculateRotation(from: LatLng, to: LatLng): Float {
    val deltaLat = to.latitude - from.latitude
    val deltaLon = to.longitude - from.longitude
    // atan2 with (deltaLon, deltaLat) gives angle from North, but we need angle from East (right)
    // since the arrow is drawn pointing right at 0° rotation
    val angleRadians = atan2(deltaLat, deltaLon)
    val angleDegrees = Math.toDegrees(angleRadians)
    // Canvas rotation is clockwise, but atan2 is counter-clockwise, so negate
    return (-angleDegrees).toFloat()
}
