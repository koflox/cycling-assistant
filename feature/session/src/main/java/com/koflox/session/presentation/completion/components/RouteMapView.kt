@file:Suppress("ComposeUnstableReceiver")

package com.koflox.session.presentation.completion.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.maps.CameraUpdateFactory
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
import com.koflox.graphics.figures.createArrowBitmap
import com.koflox.graphics.figures.createCircleBitmap
import com.koflox.designsystem.R as DesignSystemR

private const val MAP_PADDING = 100
private const val DASH_LENGTH = 20f
private const val GAP_LENGTH = 15f
private const val START_MARKER_SIZE_DP = 14
private const val END_MARKER_SIZE_DP = 20
private const val MARKER_STROKE_WIDTH_DP = 2
private val GAP_PATTERN = listOf(Dash(DASH_LENGTH), Gap(GAP_LENGTH))

@Composable
internal fun RouteMapView(
    routeDisplayData: RouteDisplayData,
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
            endMarkerRotation = endMarkerRotation,
            isSharePreview = isSharePreview,
        )
    }
}

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
@GoogleMapComposable
private fun RouteMapContent(
    routeDisplayData: RouteDisplayData,
    endMarkerRotation: Float,
    isSharePreview: Boolean = false,
) {
    val allPoints = routeDisplayData.allPoints
    val density = LocalDensity.current.density
    val startMarkerIcon = remember(density) {
        if (allPoints.isNotEmpty()) createStartMarkerIcon(density) else null
    }
    val endMarkerIcon = remember(endMarkerRotation, density) {
        if (allPoints.size >= 2) createEndMarkerIcon(density, endMarkerRotation) else null
    }
    if (allPoints.isNotEmpty() && startMarkerIcon != null) {
        Marker(
            state = rememberUpdatedMarkerState(position = allPoints.first()),
            title = "Start",
            icon = startMarkerIcon,
            anchor = Offset(0.5f, 0.5f),
            onClick = { isSharePreview },
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
            onClick = { isSharePreview },
        )
    }
}

private fun createStartMarkerIcon(density: Float) = BitmapDescriptorFactory.fromBitmap(
    createCircleBitmap(
        sizeDp = START_MARKER_SIZE_DP,
        strokeWidthDp = MARKER_STROKE_WIDTH_DP,
        fillColor = android.graphics.Color.WHITE,
        strokeColor = RouteColors.StartMarker.toArgb(),
        density = density,
    ),
)

private fun createEndMarkerIcon(density: Float, rotationDegrees: Float) = BitmapDescriptorFactory.fromBitmap(
    createArrowBitmap(
        sizeDp = END_MARKER_SIZE_DP,
        strokeWidthDp = MARKER_STROKE_WIDTH_DP,
        fillColor = android.graphics.Color.WHITE,
        strokeColor = RouteColors.EndMarker.toArgb(),
        density = density,
        rotationDegrees = rotationDegrees,
    ),
)

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
