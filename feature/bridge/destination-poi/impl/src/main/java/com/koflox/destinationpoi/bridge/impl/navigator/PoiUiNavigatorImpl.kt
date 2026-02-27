package com.koflox.destinationpoi.bridge.impl.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.destinationpoi.bridge.navigator.PoiUiNavigator
import com.koflox.poi.presentation.buttons.ActivePoiButtonsRoute

internal class PoiUiNavigatorImpl : PoiUiNavigator {

    @Composable
    override fun ActivePoiButtons(
        onPoiClicked: (query: String) -> Unit,
        onNavigateToPoiSelection: () -> Unit,
        modifier: Modifier,
    ) {
        ActivePoiButtonsRoute(
            onPoiClicked = onPoiClicked,
            onNavigateToPoiSelection = onNavigateToPoiSelection,
            modifier = modifier,
        )
    }
}
