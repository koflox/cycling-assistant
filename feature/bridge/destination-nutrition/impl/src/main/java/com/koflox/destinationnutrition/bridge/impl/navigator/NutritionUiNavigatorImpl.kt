package com.koflox.destinationnutrition.bridge.impl.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.koflox.destinationnutrition.bridge.navigator.NutritionUiNavigator
import com.koflox.nutrition.presentation.popup.NutritionPopupRoute

internal class NutritionUiNavigatorImpl : NutritionUiNavigator {

    @Composable
    override fun NutritionBreakPopup(
        suggestionTimeMs: Long,
        onDismiss: () -> Unit,
        modifier: Modifier,
    ) {
        NutritionPopupRoute(
            suggestionTimeMs = suggestionTimeMs,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}
