package com.koflox.sensor.power.presentation.testmode

internal sealed interface PowerTestModeNavigation {
    data object Back : PowerTestModeNavigation
}
