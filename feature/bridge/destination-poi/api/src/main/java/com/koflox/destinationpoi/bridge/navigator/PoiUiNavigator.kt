package com.koflox.destinationpoi.bridge.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface PoiUiNavigator {

    @Composable
    fun ActivePoiButtons(
        onPoiClicked: (query: String) -> Unit,
        modifier: Modifier,
    )
}
