package com.koflox.destinations.presentation.destinations.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.maps.model.StrokeStyle
import com.google.android.gms.maps.model.StyleSpan
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.koflox.designsystem.text.resolve
import com.koflox.designsystem.theme.CornerRadius
import com.koflox.designsystem.theme.LocalDarkTheme
import com.koflox.designsystem.theme.Spacing
import com.koflox.destinations.R
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.destinationsession.bridge.usecase.ActiveSessionRouteData
import com.koflox.destinationsession.bridge.usecase.RouteSpan
import com.koflox.graphics.figures.createCircleBitmap
import com.koflox.location.model.Location
import android.graphics.Color as AndroidColor
import com.koflox.designsystem.R as DesignSystemR

private const val DEFAULT_ZOOM_LEVEL = 12f
private const val DEFAULT_ROUTE_LINE_WIDTH = 8f
private const val USER_LOCATION_DOT_SIZE_DP = 24
private const val USER_LOCATION_STROKE_WIDTH_DP = 3
private val UserLocationBlue = Color(0xFF4285F4)

private const val ACTIVE_ROUTE_WIDTH = 10f
private const val ACTIVE_ROUTE_DASH_LENGTH = 20f
private const val ACTIVE_ROUTE_GAP_LENGTH = 15f
private const val ACTIVE_START_MARKER_SIZE_DP = 14
private const val ACTIVE_MARKER_STROKE_WIDTH_DP = 2
private val ActiveRouteGapColor = Color(0xFFBDBDBD)
private val ActiveRouteDefaultColor = Color(0xFF42A5F5)
private val ActiveRouteStartMarkerStrokeColor = Color(0xFF5A6BD5)
private val ACTIVE_GAP_PATTERN = listOf(Dash(ACTIVE_ROUTE_DASH_LENGTH), Gap(ACTIVE_ROUTE_GAP_LENGTH))

@Composable
internal fun GoogleMapView(
    modifier: Modifier = Modifier,
    selectedDestination: DestinationUiModel?,
    otherDestinations: List<DestinationUiModel>,
    userLocation: Location?,
    cameraFocusLocation: Location?,
    curvePoints: List<LatLng>,
    isSessionActive: Boolean,
    routeData: ActiveSessionRouteData? = null,
    onSelectedMarkerInfoClick: () -> Unit,
    onMapLoaded: (() -> Unit)? = null,
) {
    val isDarkTheme = LocalDarkTheme.current
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    var isMapLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(selectedDestination, isMapLoaded) {
        if (isMapLoaded) {
            selectedDestination?.let { destination ->
                moveCameraToLocation(
                    cameraPositionState = cameraPositionState,
                    location = destination.location,
                )
            }
        }
    }
    LaunchedEffect(cameraFocusLocation, isMapLoaded) {
        if (isMapLoaded && cameraFocusLocation != null && selectedDestination == null) {
            moveCameraToLocation(cameraPositionState, cameraFocusLocation)
        }
    }
    Map(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        userLocation = userLocation,
        selectedDestination = selectedDestination,
        context = context,
        otherDestinations = otherDestinations,
        curvePoints = curvePoints,
        isSessionActive = isSessionActive,
        routeData = routeData,
        onSelectedMarkerInfoClick = onSelectedMarkerInfoClick,
        isDarkTheme = isDarkTheme,
        onMapLoaded = {
            isMapLoaded = true
            onMapLoaded?.invoke()
        },
    )
}

@Composable
private fun Map(
    modifier: Modifier,
    cameraPositionState: CameraPositionState,
    userLocation: Location?,
    selectedDestination: DestinationUiModel?,
    context: Context,
    otherDestinations: List<DestinationUiModel>,
    curvePoints: List<LatLng>,
    isSessionActive: Boolean,
    routeData: ActiveSessionRouteData?,
    onSelectedMarkerInfoClick: () -> Unit,
    isDarkTheme: Boolean,
    onMapLoaded: () -> Unit,
) {
    val density = context.resources.displayMetrics.density
    val userLocationBitmap = remember(density) {
        createCircleBitmap(
            sizeDp = USER_LOCATION_DOT_SIZE_DP,
            strokeWidthDp = USER_LOCATION_STROKE_WIDTH_DP,
            fillColor = UserLocationBlue.toArgb(),
            strokeColor = AndroidColor.WHITE,
            density = density,
        )
    }
    val uiSettings = remember {
        MapUiSettings(zoomControlsEnabled = false)
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
        UserLocationMarker(userLocation, userLocationBitmap)
        selectedDestination?.let { destination ->
            Destinations(destination, otherDestinations, isSessionActive, onSelectedMarkerInfoClick)
        }
        if (curvePoints.isNotEmpty()) {
            @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
            Polyline(points = curvePoints, color = UserLocationBlue, width = DEFAULT_ROUTE_LINE_WIDTH)
        }
        routeData?.let { data ->
            @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
            ActiveSessionRouteOverlay(routeData = data, userLocation = userLocation, density = density)
        }
    }
}

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
@GoogleMapComposable
private fun UserLocationMarker(userLocation: Location?, userLocationBitmap: android.graphics.Bitmap) {
    userLocation?.let {
        val userLocationIcon = remember(userLocationBitmap) {
            BitmapDescriptorFactory.fromBitmap(userLocationBitmap)
        }
        Marker(
            state = rememberUpdatedMarkerState(position = LatLng(it.latitude, it.longitude)),
            icon = userLocationIcon,
            anchor = Offset(0.5f, 0.5f),
        )
    }
}

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
@GoogleMapComposable
private fun ActiveSessionRouteOverlay(
    routeData: ActiveSessionRouteData,
    userLocation: Location?,
    density: Float,
) {
    val startMarkerIcon = remember(density) {
        if (routeData.startPosition != null) createActiveStartMarkerIcon(density) else null
    }
    RouteSegmentPolylines(routeData)
    routeData.gapPolylines.forEach { (first, second) ->
        Polyline(
            points = listOf(LatLng(first.latitude, first.longitude), LatLng(second.latitude, second.longitude)),
            color = ActiveRouteGapColor,
            width = ACTIVE_ROUTE_WIDTH,
            pattern = ACTIVE_GAP_PATTERN,
        )
    }
    routeData.startPosition?.let { start ->
        if (startMarkerIcon != null) {
            Marker(
                state = rememberUpdatedMarkerState(position = LatLng(start.latitude, start.longitude)),
                icon = startMarkerIcon,
                anchor = Offset(0.5f, 0.5f),
            )
        }
    }
    val lastPos = routeData.lastPosition
    if (lastPos != null && userLocation != null) {
        if (routeData.isPaused) {
            Polyline(
                points = listOf(
                    LatLng(lastPos.latitude, lastPos.longitude),
                    LatLng(userLocation.latitude, userLocation.longitude),
                ),
                color = ActiveRouteGapColor,
                width = ACTIVE_ROUTE_WIDTH,
                pattern = ACTIVE_GAP_PATTERN,
            )
        } else {
            Polyline(
                points = listOf(
                    LatLng(lastPos.latitude, lastPos.longitude),
                    LatLng(userLocation.latitude, userLocation.longitude),
                ),
                color = routeData.lastSpanColorArgb?.let(::Color) ?: ActiveRouteDefaultColor,
                width = ACTIVE_ROUTE_WIDTH,
                startCap = RoundCap(),
                endCap = RoundCap(),
            )
        }
    }
}

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
@GoogleMapComposable
private fun RouteSegmentPolylines(routeData: ActiveSessionRouteData) {
    routeData.segments.forEach { segment ->
        val points = segment.points.map { LatLng(it.latitude, it.longitude) }
        val spans = segment.spans.map { span ->
            when (span) {
                is RouteSpan.Solid -> StyleSpan(StrokeStyle.colorBuilder(span.colorArgb).build(), span.length)
                is RouteSpan.Gradient -> StyleSpan(
                    StrokeStyle.gradientBuilder(span.fromColorArgb, span.toColorArgb).build(),
                    span.length,
                )
            }
        }
        Polyline(
            points = points,
            spans = spans,
            width = ACTIVE_ROUTE_WIDTH,
            startCap = RoundCap(),
            endCap = RoundCap(),
            jointType = JointType.ROUND,
        )
    }
}

private fun createActiveStartMarkerIcon(density: Float) = BitmapDescriptorFactory.fromBitmap(
    createCircleBitmap(
        sizeDp = ACTIVE_START_MARKER_SIZE_DP,
        strokeWidthDp = ACTIVE_MARKER_STROKE_WIDTH_DP,
        fillColor = AndroidColor.WHITE,
        strokeColor = ActiveRouteStartMarkerStrokeColor.toArgb(),
        density = density,
    ),
)

@Composable
private fun Destinations(
    destination: DestinationUiModel,
    otherDestinations: List<DestinationUiModel>,
    isSessionActive: Boolean,
    onSelectedMarkerInfoClick: () -> Unit,
) {
    SelectedDestinationMarker(destination, isSessionActive, onSelectedMarkerInfoClick)
    otherDestinations.forEach { otherDest ->
        OtherDestinationMarker(otherDest)
    }
}

@Composable
private fun SelectedDestinationMarker(
    destination: DestinationUiModel,
    isSessionActive: Boolean,
    onSelectedMarkerInfoClick: () -> Unit,
) {
    val markerState = rememberUpdatedMarkerState(
        position = LatLng(destination.location.latitude, destination.location.longitude),
    )
    LaunchedEffect(destination.id, isSessionActive) {
        markerState.hideInfoWindow() // need to force the info window to refresh when isSessionActive changes
        markerState.showInfoWindow()
    }
    MarkerInfoWindow(
        state = markerState,
        alpha = 1F,
        onInfoWindowClick = {
            if (!isSessionActive) onSelectedMarkerInfoClick()
        },
    ) {
        SelectedDestinationInfoWindow(destination, isSessionActive)
    }
}

@Composable
private fun OtherDestinationMarker(destination: DestinationUiModel) {
    val markerState = rememberUpdatedMarkerState(
        position = LatLng(destination.location.latitude, destination.location.longitude),
    )
    MarkerInfoWindow(
        state = markerState,
        alpha = 0.5F,
    ) {
        OtherDestinationInfoWindow(destination)
    }
}

@Composable
private fun SelectedDestinationInfoWindow(
    destination: DestinationUiModel,
    isSessionActive: Boolean,
) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(CornerRadius.Small),
            )
            .padding(Spacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = destination.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = destination.distanceFormatted.resolve(LocalContext.current),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.Tiny),
        )
        Text(
            text = stringResource(
                if (isSessionActive) R.string.info_window_heading_here else R.string.info_window_tap_for_options,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = Spacing.Small),
        )
    }
}

@Composable
private fun OtherDestinationInfoWindow(destination: DestinationUiModel) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(CornerRadius.Small),
            )
            .padding(Spacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = destination.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = destination.distanceFormatted.resolve(LocalContext.current),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.Tiny),
        )
    }
}

private suspend fun moveCameraToLocation(
    cameraPositionState: CameraPositionState,
    location: Location,
) {
    cameraPositionState.animate(
        update = CameraUpdateFactory.newLatLngZoom(
            LatLng(location.latitude, location.longitude),
            DEFAULT_ZOOM_LEVEL,
        ),
    )
}
