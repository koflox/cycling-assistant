package com.koflox.destinationnutrition.bridge.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface NutritionUiNavigator {

    @Composable
    fun NutritionBreakPopup(
        suggestionTimeMs: Long,
        onDismiss: () -> Unit,
        modifier: Modifier,
    )
}
