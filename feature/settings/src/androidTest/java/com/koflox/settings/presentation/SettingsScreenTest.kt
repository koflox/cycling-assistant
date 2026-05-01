package com.koflox.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.koflox.locale.domain.model.AppLanguage
import com.koflox.nutritionsettings.bridge.navigator.NutritionSettingsUiNavigator
import com.koflox.poisettings.bridge.navigator.PoiSettingsUiNavigator
import com.koflox.sessionsettings.bridge.navigator.StatsDisplaySettingsUiNavigator
import com.koflox.strava.api.navigator.StravaSettingsNavigator
import com.koflox.theme.domain.model.AppTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    private val testNutritionSettingsUiNavigator = object : NutritionSettingsUiNavigator {
        @Composable
        override fun NutritionSettingsSection(modifier: Modifier) = Unit
    }

    private val testPoiSettingsUiNavigator = object : PoiSettingsUiNavigator {
        @Composable
        override fun PoiSettingsSection(onNavigateToPoiSelection: () -> Unit, modifier: Modifier) = Unit

        @Composable
        override fun PoiSelectionScreen(onBackClick: () -> Unit, modifier: Modifier) = Unit
    }

    private val testStatsDisplaySettingsUiNavigator = object : StatsDisplaySettingsUiNavigator {
        @Composable
        override fun StatsDisplaySettingsSection(onNavigateToStatsConfig: () -> Unit, modifier: Modifier) = Unit

        @Composable
        override fun StatsDisplayConfigScreen(onBackClick: () -> Unit, initialSection: String?, modifier: Modifier) = Unit
    }

    private val testStravaSettingsNavigator = object : StravaSettingsNavigator {
        @Composable
        override fun StravaSettingsSection(onNavigateToConnect: () -> Unit, modifier: Modifier) = Unit
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun SettingsContentForTest(
        uiState: SettingsUiState,
        onEvent: (SettingsUiEvent) -> Unit = {},
    ) {
        SettingsContent(
            uiState = uiState,
            onBackClick = {},
            onNavigateToPoiSelection = {},
            onNavigateToStatsConfig = {},
            onNavigateToStravaConnect = {},
            onEvent = onEvent,
            nutritionSettingsUiNavigator = testNutritionSettingsUiNavigator,
            poiSettingsUiNavigator = testPoiSettingsUiNavigator,
            statsDisplaySettingsUiNavigator = testStatsDisplaySettingsUiNavigator,
            stravaSettingsNavigator = testStravaSettingsNavigator,
        )
    }

    @Test
    fun settingsScreen_displaysThemeAndLanguageLabels() {
        composeTestRule.setContent {
            SettingsContentForTest(uiState = SettingsUiState())
        }

        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysDefaultThemeValue() {
        composeTestRule.setContent {
            SettingsContentForTest(uiState = SettingsUiState(selectedTheme = AppTheme.SYSTEM))
        }

        composeTestRule.onNodeWithText("System").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysDefaultLanguageValue() {
        composeTestRule.setContent {
            SettingsContentForTest(uiState = SettingsUiState(selectedLanguage = AppLanguage.ENGLISH))
        }

        composeTestRule.onNodeWithText("English").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_themeDropdownExpanded_showsAllOptions() {
        composeTestRule.setContent {
            SettingsContentForTest(uiState = SettingsUiState(isThemeDropdownExpanded = true))
        }

        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
        // "System" appears twice: in the field and in the dropdown
        composeTestRule.onAllNodesWithText("System").assertCountEquals(2)
    }

    @Test
    fun settingsScreen_languageDropdownExpanded_showsAllOptions() {
        composeTestRule.setContent {
            SettingsContentForTest(uiState = SettingsUiState(isLanguageDropdownExpanded = true))
        }

        // "English" appears twice: in the field and in the dropdown
        composeTestRule.onAllNodesWithText("English").assertCountEquals(2)
        composeTestRule.onNodeWithText("Русский").assertIsDisplayed()
        composeTestRule.onNodeWithText("日本語").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_selectDarkTheme_updatesDisplay() {
        composeTestRule.setContent {
            SettingsContentForTest(uiState = SettingsUiState(selectedTheme = AppTheme.DARK))
        }

        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_selectJapaneseLanguage_updatesDisplay() {
        composeTestRule.setContent {
            SettingsContentForTest(uiState = SettingsUiState(selectedLanguage = AppLanguage.JAPANESE))
        }

        composeTestRule.onNodeWithText("日本語").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_clickThemeField_triggersToggleEvent() {
        var eventTriggered = false
        composeTestRule.setContent {
            SettingsContentForTest(
                uiState = SettingsUiState(),
                onEvent = { event ->
                    if (event is SettingsUiEvent.ThemeDropdownToggled) {
                        eventTriggered = true
                    }
                },
            )
        }

        composeTestRule.onNodeWithText("System").performClick()

        assert(eventTriggered) { "ThemeDropdownToggled event should be triggered" }
    }
}
