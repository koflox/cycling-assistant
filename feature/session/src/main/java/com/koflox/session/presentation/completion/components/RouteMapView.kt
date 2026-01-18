package com.koflox.session.presentation.completion.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private const val MAP_PADDING = 100
private val ROUTE_COLOR = Color(0xFF2196F3)
private const val ROUTE_WIDTH = 8f

@Composable
internal fun RouteMapView(
    routePoints: List<LatLng>,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState()
    LaunchedEffect(routePoints) {
        if (routePoints.size >= 2) {
            val boundsBuilder = LatLngBounds.Builder()
            routePoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING),
            )
        } else if (routePoints.size == 1) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(routePoints.first(), 15f),
            )
        }
    }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints,
                color = ROUTE_COLOR,
                width = ROUTE_WIDTH,
            )
        }
    }
}
