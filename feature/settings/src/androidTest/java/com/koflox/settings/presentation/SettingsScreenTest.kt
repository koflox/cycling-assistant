package com.koflox.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.koflox.locale.domain.model.AppLanguage
import com.koflox.settingsnutrition.bridge.navigator.NutritionSettingsUiNavigator
import com.koflox.theme.domain.model.AppTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    private val testNutritionSettingsUiNavigator = object : NutritionSettingsUiNavigator {
        @Composable
        override fun NutritionSettingsSection(modifier: Modifier) {
            // No-op for tests
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysThemeAndLanguageLabels() {
        composeTestRule.setContent {
            SettingsContent(
                uiState = SettingsUiState(),
                onBackClick = {},
                onEvent = {},
                nutritionSettingsUiNavigator = testNutritionSettingsUiNavigator,
            )
        }

        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysDefaultThemeValue() {
        composeTestRule.setContent {
            SettingsContent(
                uiState = SettingsUiState(selectedTheme = AppTheme.SYSTEM),
                onBackClick = {},
                onEvent = {},
                nutritionSettingsUiNavigator = testNutritionSettingsUiNavigator,
            )
        }

        composeTestRule.onNodeWithText("System").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysDefaultLanguageValue() {
        composeTestRule.setContent {
            SettingsContent(
                uiState = SettingsUiState(selectedLanguage = AppLanguage.ENGLISH),
                onBackClick = {},
                onEvent = {},
                nutritionSettingsUiNavigator = testNutritionSettingsUiNavigator,
            )
        }

        composeTestRule.onNodeWithText("English").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_themeDropdownExpanded_showsAllOptions() {
        composeTestRule.setContent {
            SettingsContent(
                uiState = SettingsUiState(isThemeDropdownExpanded = true),
                onBackClick = {},
                onEvent = {},
                nutritionSettingsUiNavigator = testNutritionSettingsUiNavigator,
            )
        }

        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
        // "System" appears twice: in the field and in the dropdown
        composeTestRule.onAllNodesWithText("System").assertCountEquals(2)
    }

    @Test
    fun settingsScreen_languageDropdownExpanded_showsAllOptions() {
        composeTestRule.setContent {
            SettingsContent(
                uiState = SettingsUiState(isLanguageDropdownExpanded = true),
                onBackClick = {},
                onEvent = {},
                nutritionSettingsUiNavigator = testNutritionSettingsUiNavigator,
            )
        }

        // "English" appears twice: in the field and in the dropdown
        composeTestRule.onAllNodesWithText("English").assertCountEquals(2)
        composeTestRule.onNodeWithText("Русский").assertIsDisplayed()
        composeTestRule.onNodeWithText("日本語").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_selectDarkTheme_updatesDisplay() {
        composeTestRule.setContent {
            SettingsContent(
                uiState = SettingsUiState(selectedTheme = AppTheme.DARK),
                onBackClick = {},
                onEvent = {},
                nutritionSettingsUiNavigator = testNutritionSettingsUiNavigator,
            )
        }

        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_selectJapaneseLanguage_updatesDisplay() {
        composeTestRule.setContent {
            SettingsContent(
                uiState = SettingsUiState(selectedLanguage = AppLanguage.JAPANESE),
                onBackClick = {},
                onEvent = {},
                nutritionSettingsUiNavigator = testNutritionSettingsUiNavigator,
            )
        }

        composeTestRule.onNodeWithText("日本語").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_clickThemeField_triggersToggleEvent() {
        var eventTriggered = false
        composeTestRule.setContent {
            SettingsContent(
                uiState = SettingsUiState(),
                onBackClick = {},
                onEvent = { event ->
                    if (event is SettingsUiEvent.ThemeDropdownToggled) {
                        eventTriggered = true
                    }
                },
                nutritionSettingsUiNavigator = testNutritionSettingsUiNavigator,
            )
        }

        composeTestRule.onNodeWithText("System").performClick()

        assert(eventTriggered) { "ThemeDropdownToggled event should be triggered" }
    }
}
