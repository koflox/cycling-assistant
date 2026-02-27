package com.koflox.destinations.presentation.destinations.components

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.maps.model.StrokeStyle
import com.google.android.gms.maps.model.StyleSpan
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
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
import com.koflox.map.ROUTE_GAP_PATTERN
import com.koflox.map.ROUTE_WIDTH
import com.koflox.map.RouteColors
import com.koflox.map.createActiveEndMarkerIcon
import com.koflox.map.createPauseMarkerIcon
import com.koflox.map.createStartMarkerIcon
import kotlin.math.pow
import android.graphics.Color as AndroidColor
import com.koflox.designsystem.R as DesignSystemR

private const val DEFAULT_ZOOM_LEVEL = 12f
private const val DEFAULT_ROUTE_LINE_WIDTH = 8f
private const val USER_LOCATION_DOT_SIZE_DP = 24
private const val USER_LOCATION_STROKE_WIDTH_DP = 3
private val UserLocationBlue = Color(0xFF4285F4)
private const val RIPPLE_BASE_RADIUS_METERS = 200.0
private const val RIPPLE_REFERENCE_ZOOM = 15f
private const val RIPPLE_DURATION_MS = 1500
private const val RIPPLE_START_ALPHA = 0.4f
private const val ROUTE_BOUNDS_PADDING_FRACTION = 0.25f
private const val DESTINATION_BOUNDS_PADDING_FRACTION = 0.15f
private const val USER_INTERACTION_COOLDOWN_MS = 10_000L
private const val SESSION_BOUNDS_PADDING_PX = 50
private const val MIN_BOUNDS_SPAN_DEGREES = 0.003

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
    SelectedDestinationCameraEffect(selectedDestination, isMapLoaded, isSessionActive, cameraPositionState)
    LaunchedEffect(cameraFocusLocation, isMapLoaded, isSessionActive) { // user location camera effect
        val isIdleWithFocusLocation = isMapLoaded && !isSessionActive && cameraFocusLocation != null
        if (isIdleWithFocusLocation && selectedDestination == null) {
            moveCameraToLocation(cameraPositionState, cameraFocusLocation)
        }
    }
    if (isSessionActive) {
        ActiveSessionCameraEffect(
            cameraPositionState = cameraPositionState,
            routeData = routeData,
            selectedDestination = selectedDestination,
            userLocation = userLocation,
        )
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

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
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
        val isRouteMarkerVisible = routeData?.startPosition != null
        if (!isRouteMarkerVisible) {
            UserLocationMarker(userLocation, userLocationBitmap)
        }
        selectedDestination?.let { destination ->
            Destinations(destination, otherDestinations, isSessionActive, onSelectedMarkerInfoClick)
        }
        DestinationGuideLine(isSessionActive, userLocation, selectedDestination, curvePoints)
        routeData?.let { data ->
            ActiveSessionRouteOverlay(
                routeData = data,
                userLocation = userLocation,
                density = density,
                cameraZoom = cameraPositionState.position.zoom,
                isDarkTheme = isDarkTheme,
            )
        }
    }
}

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

@Composable
@GoogleMapComposable
private fun DestinationGuideLine(
    isSessionActive: Boolean,
    userLocation: Location?,
    selectedDestination: DestinationUiModel?,
    curvePoints: List<LatLng>,
) {
    when {
        isSessionActive && userLocation != null && selectedDestination != null -> {
            Polyline(
                points = listOf(
                    LatLng(userLocation.latitude, userLocation.longitude),
                    LatLng(selectedDestination.location.latitude, selectedDestination.location.longitude),
                ),
                color = RouteColors.DestinationGuide,
                width = DEFAULT_ROUTE_LINE_WIDTH,
            )
        }
        curvePoints.isNotEmpty() -> {
            Polyline(points = curvePoints, color = UserLocationBlue, width = DEFAULT_ROUTE_LINE_WIDTH)
        }
    }
}

@Composable
@GoogleMapComposable
private fun ActiveSessionRouteOverlay(
    routeData: ActiveSessionRouteData,
    userLocation: Location?,
    density: Float,
    cameraZoom: Float,
    isDarkTheme: Boolean,
) {
    val startMarkerIcon = remember(density, routeData.startPosition) {
        if (routeData.startPosition != null) createStartMarkerIcon(density) else null
    }
    FirstLocationMarker(routeData, cameraZoom, isDarkTheme, startMarkerIcon)
    RouteSegmentPolylines(routeData)
    GapPolylines(routeData, userLocation)
    LastPositionMarker(routeData = routeData, userLocation = userLocation, density = density, cameraZoom = cameraZoom, isDarkTheme = isDarkTheme)
}

@Composable
private fun FirstLocationMarker(
    routeData: ActiveSessionRouteData,
    cameraZoom: Float,
    isDarkTheme: Boolean,
    startMarkerIcon: BitmapDescriptor?,
) {
    routeData.startPosition?.let { start ->
        val startLatLng = LatLng(start.latitude, start.longitude)
        if (routeData.segments.isEmpty()) {
            MarkerRipple(center = startLatLng, color = RouteColors.StartMarker, cameraZoom = cameraZoom, isDarkTheme = isDarkTheme)
        }
        if (startMarkerIcon != null) {
            Marker(
                state = rememberUpdatedMarkerState(position = startLatLng),
                icon = startMarkerIcon,
                anchor = Offset(0.5f, 0.5f),
            )
        }
    }
}

@Composable
@GoogleMapComposable
private fun LastPositionMarker(
    routeData: ActiveSessionRouteData,
    userLocation: Location?,
    density: Float,
    cameraZoom: Float,
    isDarkTheme: Boolean,
) {
    routeData.lastPosition?.let { lastPos ->
        val endLatLng = LatLng(lastPos.latitude, lastPos.longitude)
        if (routeData.isPaused) {
            val pauseRippleCenter = userLocation?.let { LatLng(it.latitude, it.longitude) } ?: endLatLng
            val pauseMarkerIcon = remember(density) { createPauseMarkerIcon(density) }
            MarkerRipple(center = pauseRippleCenter, color = RouteColors.PauseMarker, cameraZoom = cameraZoom, isDarkTheme = isDarkTheme)
            Marker(
                state = rememberUpdatedMarkerState(position = pauseRippleCenter),
                icon = pauseMarkerIcon,
                anchor = Offset(0.5f, 0.5f),
            )
        } else if (routeData.segments.isNotEmpty()) {
            val activeEndMarkerIcon = remember(density, routeData.lastBearingDegrees) {
                createActiveEndMarkerIcon(density, routeData.lastBearingDegrees ?: 0f)
            }
            MarkerRipple(center = endLatLng, color = RouteColors.ActiveEndMarker, cameraZoom = cameraZoom, isDarkTheme = isDarkTheme)
            Marker(
                state = rememberUpdatedMarkerState(position = endLatLng),
                icon = activeEndMarkerIcon,
                anchor = Offset(0.5f, 0.5f),
            )
        }
    }
}

@Composable
@GoogleMapComposable
private fun GapPolylines(routeData: ActiveSessionRouteData, userLocation: Location?) {
    routeData.gapPolylines.forEach { (first, second) ->
        Polyline(
            points = listOf(LatLng(first.latitude, first.longitude), LatLng(second.latitude, second.longitude)),
            color = RouteColors.Gap,
            width = ROUTE_WIDTH,
            pattern = ROUTE_GAP_PATTERN,
        )
    }
    val lastPos = routeData.lastPosition
    if (lastPos != null && userLocation != null && routeData.showGapToUserLocation) {
        Polyline(
            points = listOf(LatLng(lastPos.latitude, lastPos.longitude), LatLng(userLocation.latitude, userLocation.longitude)),
            color = RouteColors.Gap,
            width = ROUTE_WIDTH,
            pattern = ROUTE_GAP_PATTERN,
        )
    }
}

@Composable
@GoogleMapComposable
private fun MarkerRipple(center: LatLng, color: Color, cameraZoom: Float, isDarkTheme: Boolean) {
    val rippleColor = if (isDarkTheme) Color.White else color
    val transition = rememberInfiniteTransition(label = "ripple")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = RIPPLE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rippleProgress",
    )
    val zoomScale = 2.0.pow((RIPPLE_REFERENCE_ZOOM - cameraZoom).toDouble())
    Circle(
        center = center,
        radius = RIPPLE_BASE_RADIUS_METERS * zoomScale * progress,
        fillColor = rippleColor.copy(alpha = RIPPLE_START_ALPHA * (1f - progress)),
        strokeWidth = 0f,
    )
}

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
            width = ROUTE_WIDTH,
            startCap = RoundCap(),
            endCap = RoundCap(),
            jointType = JointType.ROUND,
        )
    }
}

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

@Composable
private fun SelectedDestinationCameraEffect(
    selectedDestination: DestinationUiModel?,
    isMapLoaded: Boolean,
    isSessionActive: Boolean,
    cameraPositionState: CameraPositionState,
) {
    LaunchedEffect(selectedDestination, isMapLoaded, isSessionActive) {
        if (isMapLoaded && !isSessionActive && selectedDestination != null) {
            moveCameraToLocation(
                cameraPositionState = cameraPositionState,
                location = selectedDestination.location,
            )
        }
    }
}

@Composable
private fun ActiveSessionCameraEffect(
    cameraPositionState: CameraPositionState,
    routeData: ActiveSessionRouteData?,
    selectedDestination: DestinationUiModel?,
    userLocation: Location?,
) {
    var lastGestureTimeMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        snapshotFlow { cameraPositionState.isMoving to cameraPositionState.cameraMoveStartedReason }
            .collect { (isMoving, reason) ->
                if (isMoving && reason == CameraMoveStartedReason.GESTURE) {
                    lastGestureTimeMs = System.currentTimeMillis()
                }
            }
    }
    LaunchedEffect(routeData, userLocation) {
        if (System.currentTimeMillis() - lastGestureTimeMs < USER_INTERACTION_COOLDOWN_MS) return@LaunchedEffect
        val bounds = computeSessionBounds(routeData, selectedDestination, userLocation) ?: return@LaunchedEffect
        cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, SESSION_BOUNDS_PADDING_PX))
    }
}

private fun computeSessionBounds(
    routeData: ActiveSessionRouteData?,
    selectedDestination: DestinationUiModel?,
    userLocation: Location?,
): LatLngBounds? {
    val points = mutableListOf<LatLng>()
    userLocation?.let { points.add(LatLng(it.latitude, it.longitude)) }
    if (selectedDestination != null) {
        points.add(LatLng(selectedDestination.location.latitude, selectedDestination.location.longitude))
    } else {
        routeData?.startPosition?.let { points.add(LatLng(it.latitude, it.longitude)) }
        routeData?.lastPosition?.let { points.add(LatLng(it.latitude, it.longitude)) }
        routeData?.segments?.forEach { segment ->
            segment.points.forEach { points.add(LatLng(it.latitude, it.longitude)) }
        }
    }
    if (points.size < 2) return null
    val builder = LatLngBounds.Builder()
    points.forEach { builder.include(it) }
    val fraction = if (selectedDestination != null) DESTINATION_BOUNDS_PADDING_FRACTION else ROUTE_BOUNDS_PADDING_FRACTION
    return builder.build().expand(fraction)
}

private fun LatLngBounds.expand(fraction: Float): LatLngBounds {
    val latSpan = maxOf(northeast.latitude - southwest.latitude, MIN_BOUNDS_SPAN_DEGREES)
    val lngSpan = maxOf(northeast.longitude - southwest.longitude, MIN_BOUNDS_SPAN_DEGREES)
    val latPad = latSpan * fraction
    val lngPad = lngSpan * fraction
    return LatLngBounds(
        LatLng(southwest.latitude - latPad, southwest.longitude - lngPad),
        LatLng(northeast.latitude + latPad, northeast.longitude + lngPad),
    )
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
