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
    startMarkerRotation: Float,
    endMarkerRotation: Float,
    modifier: Modifier = Modifier,
    isSharePreview: Boolean = false,
    onMapLoaded: (() -> Unit)? = null,
) {
    val isDarkTheme = LocalDarkTheme.current
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val markerSizePx = with(LocalDensity.current) { 24.dp.toPx().toInt() }
    LaunchedEffect(routePoints) {
        animateCameraToRoute(routePoints, cameraPositionState)
    }
    val uiSettings = remember(isSharePreview) {
        MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            scrollGesturesEnabled = !isSharePreview,
            zoomGesturesEnabled = !isSharePreview,
            tiltGesturesEnabled = !isSharePreview,
            rotationGesturesEnabled = !isSharePreview,
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
        val startMarkerIcon = remember(startMarkerRotation, markerSizePx) {
            if (routePoints.isNotEmpty()) createArrowBitmap(markerSizePx, START_MARKER_COLOR, startMarkerRotation) else null
        }
        val endMarkerIcon = remember(endMarkerRotation, markerSizePx) {
            if (routePoints.size >= 2) createArrowBitmap(markerSizePx, END_MARKER_COLOR, endMarkerRotation) else null
        }
        if (routePoints.isNotEmpty() && startMarkerIcon != null) {
            Marker(
                state = rememberUpdatedMarkerState(position = routePoints.first()),
                title = "Start",
                icon = startMarkerIcon,
                anchor = Offset(0.5f, 0.5f),
            )
        }
        if (routePoints.size >= 2 && endMarkerIcon != null) {
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
