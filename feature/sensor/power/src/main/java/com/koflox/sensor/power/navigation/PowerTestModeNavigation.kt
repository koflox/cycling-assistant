package com.koflox.sensor.power.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.koflox.sensor.power.presentation.testmode.PowerTestModeRoute

const val MAC_ADDRESS_ARG = "mac_address"
const val POWER_TEST_MODE_ROUTE = "power_test_mode/{$MAC_ADDRESS_ARG}"

fun NavGraphBuilder.powerTestModeScreen(
    onBackClick: () -> Unit,
) {
    composable(
        route = POWER_TEST_MODE_ROUTE,
        arguments = listOf(
            navArgument(MAC_ADDRESS_ARG) { type = NavType.StringType },
        ),
    ) {
        PowerTestModeRoute(onBackClick = onBackClick)
    }
}
