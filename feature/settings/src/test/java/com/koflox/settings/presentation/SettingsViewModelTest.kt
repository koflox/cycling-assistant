package com.koflox.settings.presentation

import app.cash.turbine.test
import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import com.koflox.settings.domain.usecase.ObserveSettingsUseCase
import com.koflox.settings.domain.usecase.UpdateSettingsUseCase
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeSettingsUseCase: ObserveSettingsUseCase = mockk()
    private val updateSettingsUseCase: UpdateSettingsUseCase = mockk(relaxed = true)

    private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
    private val languageFlow = MutableStateFlow(AppLanguage.ENGLISH)

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        every { observeSettingsUseCase.observeTheme() } returns themeFlow
        every { observeSettingsUseCase.observeLanguage() } returns languageFlow
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            observeSettingsUseCase = observeSettingsUseCase,
            updateSettingsUseCase = updateSettingsUseCase,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `initial state has default values`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(AppTheme.SYSTEM, state.selectedTheme)
            assertEquals(AppLanguage.ENGLISH, state.selectedLanguage)
            assertFalse(state.isThemeDropdownExpanded)
            assertFalse(state.isLanguageDropdownExpanded)
        }
    }

    @Test
    fun `observeSettings updates theme from flow`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            themeFlow.value = AppTheme.DARK

            val updatedState = awaitItem()
            assertEquals(AppTheme.DARK, updatedState.selectedTheme)
        }
    }

    @Test
    fun `observeSettings updates language from flow`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            languageFlow.value = AppLanguage.RUSSIAN

            val updatedState = awaitItem()
            assertEquals(AppLanguage.RUSSIAN, updatedState.selectedLanguage)
        }
    }

    @Test
    fun `ThemeSelected updates theme and closes dropdown`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onEvent(SettingsUiEvent.ThemeDropdownToggled)
            val expandedState = awaitItem()
            assertTrue(expandedState.isThemeDropdownExpanded)

            viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK))

            val closedState = awaitItem()
            assertFalse(closedState.isThemeDropdownExpanded)
        }

        coVerify { updateSettingsUseCase.updateTheme(AppTheme.DARK) }
    }

    @Test
    fun `LanguageSelected updates language and closes dropdown`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onEvent(SettingsUiEvent.LanguageDropdownToggled)
            awaitItem() // Expanded

            viewModel.onEvent(SettingsUiEvent.LanguageSelected(AppLanguage.JAPANESE))

            val closedState = awaitItem()
            assertFalse(closedState.isLanguageDropdownExpanded)
        }

        coVerify { updateSettingsUseCase.updateLanguage(AppLanguage.JAPANESE) }
    }

    @Test
    fun `ThemeDropdownToggled opens dropdown`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onEvent(SettingsUiEvent.ThemeDropdownToggled)

            val expandedState = awaitItem()
            assertTrue(expandedState.isThemeDropdownExpanded)
            assertFalse(expandedState.isLanguageDropdownExpanded)
        }
    }

    @Test
    fun `ThemeDropdownToggled closes dropdown when already open`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onEvent(SettingsUiEvent.ThemeDropdownToggled)
            awaitItem() // Expanded

            viewModel.onEvent(SettingsUiEvent.ThemeDropdownToggled)

            val closedState = awaitItem()
            assertFalse(closedState.isThemeDropdownExpanded)
        }
    }

    @Test
    fun `ThemeDropdownToggled closes language dropdown when opening theme`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onEvent(SettingsUiEvent.LanguageDropdownToggled)
            awaitItem() // Language expanded

            viewModel.onEvent(SettingsUiEvent.ThemeDropdownToggled)

            val state = awaitItem()
            assertTrue(state.isThemeDropdownExpanded)
            assertFalse(state.isLanguageDropdownExpanded)
        }
    }

    @Test
    fun `LanguageDropdownToggled opens dropdown`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onEvent(SettingsUiEvent.LanguageDropdownToggled)

            val expandedState = awaitItem()
            assertTrue(expandedState.isLanguageDropdownExpanded)
            assertFalse(expandedState.isThemeDropdownExpanded)
        }
    }

    @Test
    fun `LanguageDropdownToggled closes dropdown when already open`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onEvent(SettingsUiEvent.LanguageDropdownToggled)
            awaitItem() // Expanded

            viewModel.onEvent(SettingsUiEvent.LanguageDropdownToggled)

            val closedState = awaitItem()
            assertFalse(closedState.isLanguageDropdownExpanded)
        }
    }

    @Test
    fun `LanguageDropdownToggled closes theme dropdown when opening language`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onEvent(SettingsUiEvent.ThemeDropdownToggled)
            awaitItem() // Theme expanded

            viewModel.onEvent(SettingsUiEvent.LanguageDropdownToggled)

            val state = awaitItem()
            assertTrue(state.isLanguageDropdownExpanded)
            assertFalse(state.isThemeDropdownExpanded)
        }
    }

    @Test
    fun `DropdownsDismissed closes all dropdowns`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.onEvent(SettingsUiEvent.ThemeDropdownToggled)
            awaitItem() // Theme expanded

            viewModel.onEvent(SettingsUiEvent.DropdownsDismissed)

            val closedState = awaitItem()
            assertFalse(closedState.isThemeDropdownExpanded)
            assertFalse(closedState.isLanguageDropdownExpanded)
        }
    }

    @Test
    fun `availableThemes contains all theme options`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(AppTheme.entries, state.availableThemes)
        }
    }

    @Test
    fun `availableLanguages contains all language options`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(AppLanguage.entries, state.availableLanguages)
        }
    }
}
