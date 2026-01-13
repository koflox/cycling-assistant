package com.koflox.destinations.presentation.destinations.components

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
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
            Destinations(context, destination, otherDestinations)
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
    context: Context,
    destination: DestinationUiModel,
    otherDestinations: List<DestinationUiModel>,
) {
    val setMarker: @Composable (DestinationUiModel) -> Unit = { destination ->
        val snippet = if (destination.isMain) {
            String.format(
                Locale.getDefault(),
                "${stringResource(R.string.distance_to_dest_desc)} - ${stringResource(R.string.get_route_hint_for_google_maps)}",
                destination.distanceKm,
            )
        } else {
            String.format(
                Locale.getDefault(),
                stringResource(R.string.distance_to_dest_desc),
                destination.distanceKm,
            )
        }
        val alpha = if (destination.isMain) 1F else 0.5F
        val onInfoWindowClick: (Marker) -> Unit = {
            if (destination.isMain) openInGoogleMaps(context, destination) else Unit
        }
        val markerState = rememberUpdatedMarkerState(
            position = LatLng(
                destination.location.latitude,
                destination.location.longitude,
            ),
        )
        Marker(
            state = markerState,
            title = destination.title,
            snippet = snippet,
            alpha = alpha,
            onInfoWindowClick = onInfoWindowClick,
        )
        if (destination.isMain) markerState.showInfoWindow()
    }
    setMarker(destination)
    otherDestinations.forEach { destination ->
        setMarker(destination)
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

// TODO: move to VM and check the package presence before starting the activity
private fun openInGoogleMaps(context: Context, destination: DestinationUiModel) {
    val uri = "google.navigation:q=${destination.location.latitude},${destination.location.longitude}&mode=b".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    context.startActivity(intent)
}
