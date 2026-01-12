package com.koflox.cyclingassistant.presentation.destinations.components

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.koflox.cyclingassistant.R
import com.koflox.cyclingassistant.domain.model.Location
import com.koflox.cyclingassistant.presentation.destinations.model.DestinationUiModel
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private const val DEFAULT_ZOOM_LEVEL = 12f
private const val DEFAULT_BOUNDS_PADDING = 150

@Composable
internal fun GoogleMapView(
    modifier: Modifier = Modifier,
    selectedDestination: DestinationUiModel?,
    otherDestinations: List<DestinationUiModel>,
    userLocation: Location?,
) {
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(selectedDestination) {
        selectedDestination?.let { destination ->
            if (userLocation != null) {
                val southWest = LatLng(
                    min(userLocation.latitude, destination.location.latitude),
                    min(userLocation.longitude, destination.location.longitude),
                )
                val northEast = LatLng(
                    max(userLocation.latitude, destination.location.latitude),
                    max(userLocation.longitude, destination.location.longitude),
                )
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(
                        LatLngBounds(southWest, northEast),
                        DEFAULT_BOUNDS_PADDING,
                    ),
                )
            } else {
                moveCameraWithNewLatLngZoom(cameraPositionState, destination.location)
            }
        }
    }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapLoaded = {
            coroutineScope.launch {
                userLocation?.let { destination ->
                    moveCameraWithNewLatLngZoom(cameraPositionState, destination)
                }
            }
        }
    ) {
        userLocation?.let {
            Marker(
                state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                title = "You are here",
            )
        }
        selectedDestination?.let { destination ->
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
    }
}

private suspend fun moveCameraWithNewLatLngZoom(
    cameraPositionState: CameraPositionState,
    destination: Location
) {
    cameraPositionState.animate(
        update = CameraUpdateFactory.newLatLngZoom(
            LatLng(destination.latitude, destination.longitude),
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
