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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.koflox.destinations.R
import com.koflox.destinations.presentation.destinations.model.DestinationUiModel
import com.koflox.graphics.curves.createCurvePoints
import com.koflox.graphics.figures.createCircleBitmap
import com.koflox.graphics.primitives.Point
import com.koflox.location.model.Location
import java.util.Locale
import android.graphics.Color as AndroidColor

private const val DEFAULT_ZOOM_LEVEL = 12f
private const val DEFAULT_ROUTE_LINE_WIDTH = 8f
private const val USER_LOCATION_DOT_SIZE_DP = 24
private const val USER_LOCATION_STROKE_WIDTH_DP = 3
private val UserLocationBlue = Color(0xFF4285F4)

@Composable
internal fun GoogleMapView(
    modifier: Modifier = Modifier,
    selectedDestination: DestinationUiModel?,
    otherDestinations: List<DestinationUiModel>,
    userLocation: Location?,
    cameraFocusLocation: Location?,
    onSelectedMarkerInfoClick: () -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current

    LaunchedEffect(selectedDestination) {
        selectedDestination?.let { destination ->
            moveCameraToLocation(
                cameraPositionState = cameraPositionState,
                location = destination.location,
            )
        }
    }
    LaunchedEffect(cameraFocusLocation) {
        if (cameraFocusLocation != null && selectedDestination == null) {
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
        onSelectedMarkerInfoClick = onSelectedMarkerInfoClick,
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
    onSelectedMarkerInfoClick: () -> Unit,
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
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        userLocation?.let {
            val userLocationIcon = remember(userLocationBitmap) {
                BitmapDescriptorFactory.fromBitmap(userLocationBitmap)
            }
            val userLocationMarkerState = rememberUpdatedMarkerState(
                position = LatLng(it.latitude, it.longitude),
            )
            Marker(
                state = userLocationMarkerState,
                icon = userLocationIcon,
                anchor = Offset(0.5f, 0.5f),
            )
        }
        selectedDestination?.let { destination ->
            Destinations(destination, otherDestinations, onSelectedMarkerInfoClick)
        }
        if (userLocation != null && selectedDestination != null) {
            val curvePoints = createCurvePoints(
                start = Point(userLocation.latitude, userLocation.longitude),
                end = Point(selectedDestination.location.latitude, selectedDestination.location.longitude),
            ).map { LatLng(it.x, it.y) }
            Polyline(
                points = curvePoints,
                color = UserLocationBlue,
                width = DEFAULT_ROUTE_LINE_WIDTH,
            )
        }
    }
}

@Composable
private fun Destinations(
    destination: DestinationUiModel,
    otherDestinations: List<DestinationUiModel>,
    onSelectedMarkerInfoClick: () -> Unit,
) {
    SelectedDestinationMarker(destination, onSelectedMarkerInfoClick)
    otherDestinations.forEach { otherDest ->
        OtherDestinationMarker(otherDest)
    }
}

@Composable
private fun SelectedDestinationMarker(
    destination: DestinationUiModel,
    onSelectedMarkerInfoClick: () -> Unit,
) {
    val markerState = rememberUpdatedMarkerState(
        position = LatLng(destination.location.latitude, destination.location.longitude),
    )
    LaunchedEffect(destination.id) {
        markerState.showInfoWindow()
    }
    MarkerInfoWindow(
        state = markerState,
        alpha = 1F,
        onInfoWindowClick = { onSelectedMarkerInfoClick() },
    ) {
        SelectedDestinationInfoWindow(destination)
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
private fun SelectedDestinationInfoWindow(destination: DestinationUiModel) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = destination.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = String.format(
                Locale.getDefault(),
                stringResource(R.string.distance_to_dest_desc),
                destination.distanceKm,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = stringResource(R.string.info_window_tap_for_options),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun OtherDestinationInfoWindow(destination: DestinationUiModel) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = destination.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = String.format(
                Locale.getDefault(),
                stringResource(R.string.distance_to_dest_desc),
                destination.distanceKm,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
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
