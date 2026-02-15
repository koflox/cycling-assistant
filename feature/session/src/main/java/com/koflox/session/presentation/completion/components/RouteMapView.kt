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
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.koflox.designsystem.theme.LocalDarkTheme
import com.koflox.designsystem.R as DesignSystemR

private const val MAP_PADDING = 100
private const val DASH_LENGTH = 20f
private const val GAP_LENGTH = 15f
private val MARKER_SIZE_DP = 24.dp
private val GAP_PATTERN = listOf(Dash(DASH_LENGTH), Gap(GAP_LENGTH))

@Composable
internal fun RouteMapView(
    routeDisplayData: RouteDisplayData,
    startMarkerRotation: Float,
    endMarkerRotation: Float,
    modifier: Modifier = Modifier,
    isSharePreview: Boolean = false,
    onMapLoaded: (() -> Unit)? = null,
) {
    val isDarkTheme = LocalDarkTheme.current
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    LaunchedEffect(routeDisplayData) {
        animateCameraToRoute(routeDisplayData.allPoints, cameraPositionState)
    }
    val uiSettings = remember(isSharePreview) { buildMapUiSettings(isSharePreview) }
    val mapProperties = remember(isDarkTheme) {
        MapProperties(
            mapStyleOptions = if (isDarkTheme) MapStyleOptions.loadRawResourceStyle(context, DesignSystemR.raw.map_style_dark) else null,
        )
    }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = mapProperties,
        onMapLoaded = onMapLoaded,
    ) {
        RouteMapContent(
            routeDisplayData = routeDisplayData,
            startMarkerRotation = startMarkerRotation,
            endMarkerRotation = endMarkerRotation,
        )
    }
}

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
@GoogleMapComposable
private fun RouteMapContent(
    routeDisplayData: RouteDisplayData,
    startMarkerRotation: Float,
    endMarkerRotation: Float,
) {
    val allPoints = routeDisplayData.allPoints
    val markerSizePx = with(LocalDensity.current) { MARKER_SIZE_DP.toPx().toInt() }
    val startMarkerIcon = remember(startMarkerRotation, markerSizePx) {
        if (allPoints.isNotEmpty()) createArrowBitmap(markerSizePx, RouteColors.StartMarker, startMarkerRotation) else null
    }
    val endMarkerIcon = remember(endMarkerRotation, markerSizePx) {
        if (allPoints.size >= 2) createArrowBitmap(markerSizePx, RouteColors.EndMarker, endMarkerRotation) else null
    }
    if (allPoints.isNotEmpty() && startMarkerIcon != null) {
        Marker(
            state = rememberUpdatedMarkerState(position = allPoints.first()),
            title = "Start",
            icon = startMarkerIcon,
            anchor = Offset(0.5f, 0.5f),
        )
    }
    routeDisplayData.segments.forEach { segment ->
        Polyline(
            points = segment.points,
            spans = segment.spans,
            width = ROUTE_WIDTH,
            startCap = RoundCap(),
            endCap = RoundCap(),
            jointType = JointType.ROUND,
        )
    }
    routeDisplayData.gapPolylines.forEach { points ->
        Polyline(
            points = points,
            color = RouteColors.Gap,
            width = ROUTE_WIDTH,
            pattern = GAP_PATTERN,
        )
    }
    if (allPoints.size >= 2 && endMarkerIcon != null) {
        Marker(
            state = rememberUpdatedMarkerState(position = allPoints.last()),
            title = "End",
            icon = endMarkerIcon,
            anchor = Offset(0.5f, 0.5f),
        )
    }
}

private fun buildMapUiSettings(isSharePreview: Boolean) = MapUiSettings(
    zoomControlsEnabled = false,
    mapToolbarEnabled = false,
    scrollGesturesEnabled = !isSharePreview,
    zoomGesturesEnabled = !isSharePreview,
    tiltGesturesEnabled = !isSharePreview,
    rotationGesturesEnabled = !isSharePreview,
)

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
    val vertices = computeArrowVertices(sizePx.toFloat())
    canvas.withRotation(rotationDegrees, sizePx / 2f, sizePx / 2f) {
        val path = Path().apply {
            moveTo(vertices.tipX, vertices.tipY)
            lineTo(vertices.baseUpperX, vertices.baseUpperY)
            lineTo(vertices.baseLowerX, vertices.baseLowerY)
            close()
        }
        drawPath(path, paint)
    }
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
