package com.koflox.destinations.presentation.destinations.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class DestinationComponentsTest {

    companion object {
        private const val DISTANCE_KM = 15.0
        private const val TOLERANCE_KM = 2.5
        private const val MIN_DISTANCE_KM = 5.0
        private const val MAX_DISTANCE_KM = 30.0
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun statusCard_displaysMessage() {
        composeTestRule.setContent {
            StatusCard(message = "Preparing the destinations")
        }

        composeTestRule.onNodeWithText("Preparing the destinations").assertIsDisplayed()
    }

    @Test
    fun routeSlider_displaysDistanceTitle() {
        composeTestRule.setContent {
            RouteSlider(
                distanceKm = DISTANCE_KM,
                toleranceKm = TOLERANCE_KM,
                minDistanceKm = MIN_DISTANCE_KM,
                maxDistanceKm = MAX_DISTANCE_KM,
                onDistanceChanged = {},
            )
        }

        composeTestRule.onNodeWithText("One way distance").assertIsDisplayed()
    }

    @Test
    fun routeSlider_displaysDistanceValue() {
        composeTestRule.setContent {
            RouteSlider(
                distanceKm = DISTANCE_KM,
                toleranceKm = TOLERANCE_KM,
                minDistanceKm = MIN_DISTANCE_KM,
                maxDistanceKm = MAX_DISTANCE_KM,
                onDistanceChanged = {},
            )
        }

        composeTestRule.onNodeWithText("15 km (±2.5)", substring = true).assertIsDisplayed()
    }

    @Test
    fun letsGoButton_displaysButtonText() {
        composeTestRule.setContent {
            LetsGoButton(
                onClick = {},
                enabled = true,
            )
        }

        composeTestRule.onNodeWithText("Let's go!").assertIsDisplayed()
    }

    @Test
    fun letsGoButton_enabled_triggersOnClick() {
        var clicked = false
        composeTestRule.setContent {
            LetsGoButton(
                onClick = { clicked = true },
                enabled = true,
            )
        }

        composeTestRule.onNodeWithText("Let's go!").performClick()

        assert(clicked) { "onClick should be triggered when button is enabled" }
    }

    @Test
    fun letsGoButton_disabled_doesNotTriggerOnClick() {
        var clicked = false
        composeTestRule.setContent {
            LetsGoButton(
                onClick = { clicked = true },
                enabled = false,
            )
        }

        composeTestRule.onNodeWithText("Let's go!").performClick()

        assert(!clicked) { "onClick should not be triggered when button is disabled" }
    }
}
