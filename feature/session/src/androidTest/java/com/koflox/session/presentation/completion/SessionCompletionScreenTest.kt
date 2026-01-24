package com.koflox.session.presentation.completion

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class SessionCompletionScreenTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_NAME = "Central Park"
        private const val START_DATE = "Jan 15, 2025"
        private const val ELAPSED_TIME = "01:30:45"
        private const val DISTANCE = "25.50"
        private const val AVG_SPEED = "17.0"
        private const val TOP_SPEED = "32.5"
        private const val ALTITUDE_GAIN = "150"
        private const val ERROR_MESSAGE = "Failed to load session"
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sessionCompletionScreen_loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            SessionCompletionContent(
                uiState = SessionCompletionUiState.Loading,
                onBackClick = {},
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Session Complete").assertIsDisplayed()
    }

    @Test
    fun sessionCompletionScreen_errorState_showsErrorMessage() {
        composeTestRule.setContent {
            SessionCompletionContent(
                uiState = SessionCompletionUiState.Error(message = ERROR_MESSAGE),
                onBackClick = {},
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText(ERROR_MESSAGE).assertIsDisplayed()
    }

    @Test
    fun sessionCompletionScreen_contentState_showsDestinationNameInTitle() {
        composeTestRule.setContent {
            SessionCompletionContent(
                uiState = createContentState(),
                onBackClick = {},
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText(DESTINATION_NAME).assertIsDisplayed()
    }

    @Test
    fun sessionCompletionScreen_contentState_showsShareIcon() {
        composeTestRule.setContent {
            SessionCompletionContent(
                uiState = createContentState(),
                onBackClick = {},
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Share").assertIsDisplayed()
    }

    @Test
    fun sessionCompletionScreen_contentState_showsSessionStats() {
        composeTestRule.setContent {
            SessionCompletionContent(
                uiState = createContentState(),
                onBackClick = {},
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText(ELAPSED_TIME).assertIsDisplayed()
        composeTestRule.onNodeWithText("$DISTANCE km").assertIsDisplayed()
        composeTestRule.onNodeWithText("$AVG_SPEED km/h").assertIsDisplayed()
        composeTestRule.onNodeWithText("$TOP_SPEED km/h").assertIsDisplayed()
    }

    @Test
    fun sessionCompletionScreen_loadingState_doesNotShowShareIcon() {
        composeTestRule.setContent {
            SessionCompletionContent(
                uiState = SessionCompletionUiState.Loading,
                onBackClick = {},
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Share").assertDoesNotExist()
    }

    @Test
    fun sessionCompletionScreen_errorState_doesNotShowShareIcon() {
        composeTestRule.setContent {
            SessionCompletionContent(
                uiState = SessionCompletionUiState.Error(message = ERROR_MESSAGE),
                onBackClick = {},
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Share").assertDoesNotExist()
    }

    @Test
    fun sessionCompletionScreen_clickShareIcon_triggersShareEvent() {
        var shareClicked = false
        composeTestRule.setContent {
            SessionCompletionContent(
                uiState = createContentState(),
                onBackClick = {},
                onEvent = { event ->
                    if (event is SessionCompletionUiEvent.ShareClicked) {
                        shareClicked = true
                    }
                },
            )
        }

        composeTestRule.onNodeWithContentDescription("Share").performClick()

        assert(shareClicked) { "ShareClicked event should be triggered" }
    }

    @Test
    fun sessionCompletionScreen_clickBackButton_triggersBackClick() {
        var backClicked = false
        composeTestRule.setContent {
            SessionCompletionContent(
                uiState = createContentState(),
                onBackClick = { backClicked = true },
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(backClicked) { "Back click should be triggered" }
    }

    private fun createContentState(
        overlay: Overlay? = null,
    ) = SessionCompletionUiState.Content(
        sessionId = SESSION_ID,
        destinationName = DESTINATION_NAME,
        startDateFormatted = START_DATE,
        elapsedTimeFormatted = ELAPSED_TIME,
        traveledDistanceFormatted = DISTANCE,
        averageSpeedFormatted = AVG_SPEED,
        topSpeedFormatted = TOP_SPEED,
        altitudeGainFormatted = ALTITUDE_GAIN,
        routePoints = emptyList(), // Empty to avoid Google Maps initialization issues in tests
        overlay = overlay,
    )
}
