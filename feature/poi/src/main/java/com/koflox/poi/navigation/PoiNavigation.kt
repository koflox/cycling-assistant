package com.koflox.poi.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.koflox.poi.presentation.selection.PoiSelectionRoute

const val POI_SELECTION_ROUTE = "poi_selection"

fun NavGraphBuilder.poiSelectionScreen(
    onBackClick: () -> Unit,
) {
    composable(route = POI_SELECTION_ROUTE) {
        PoiSelectionRoute(
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
