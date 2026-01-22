package com.koflox.settings.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.koflox.settings.presentation.SettingsRoute

const val SETTINGS_ROUTE = "settings"

fun NavGraphBuilder.settingsScreen(
    onBackClick: () -> Unit,
) {
    composable(route = SETTINGS_ROUTE) {
        SettingsRoute(
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
