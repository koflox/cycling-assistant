package com.koflox.destinations.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.koflox.destinations.presentation.destinations.DestinationsScreen

const val DESTINATIONS_ROUTE = "destinations"

fun NavGraphBuilder.destinationsScreen() {
    composable(route = DESTINATIONS_ROUTE) {
        DestinationsScreen()
    }
}
