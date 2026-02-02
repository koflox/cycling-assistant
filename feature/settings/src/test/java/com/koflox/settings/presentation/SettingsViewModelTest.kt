package com.koflox.settings.presentation

import app.cash.turbine.test
import com.koflox.settings.domain.model.AppLanguage
import com.koflox.settings.domain.model.AppTheme
import com.koflox.settings.domain.model.InvalidWeightException
import com.koflox.settings.domain.usecase.ObserveSettingsUseCase
import com.koflox.settings.domain.usecase.UpdateSettingsUseCase
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    companion object {
        private const val MIN_WEIGHT_KG = 1
        private const val MAX_WEIGHT_KG = 300
    }

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
        coEvery { observeSettingsUseCase.getRiderWeightKg() } returns null
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
    fun `initial state loads rider weight`() = runTest {
        coEvery { observeSettingsUseCase.getRiderWeightKg() } returns 80f

        viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial state with default weight

            val state = awaitItem()
            assertEquals("80", state.riderWeightKg)
        }
    }

    @Test
    fun `initial state shows empty weight when not set`() = runTest {
        coEvery { observeSettingsUseCase.getRiderWeightKg() } returns null

        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.riderWeightKg)
        }
    }

    @Test
    fun `RiderWeightChanged updates text and persists valid weight`() = runTest {
        coEvery { updateSettingsUseCase.updateRiderWeightKg("82.5") } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.RiderWeightChanged("82.5"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("82.5", state.riderWeightKg)
        assertNull(state.riderWeightError)
        coVerify { updateSettingsUseCase.updateRiderWeightKg("82.5") }
    }

    @Test
    fun `RiderWeightChanged with invalid input shows error with weight bounds`() = runTest {
        coEvery {
            updateSettingsUseCase.updateRiderWeightKg("abc")
        } returns Result.failure(InvalidWeightException(MIN_WEIGHT_KG.toDouble(), MAX_WEIGHT_KG.toDouble()))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.RiderWeightChanged("abc"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("abc", state.riderWeightKg)
        assertEquals(RiderWeightError(MIN_WEIGHT_KG, MAX_WEIGHT_KG), state.riderWeightError)
    }

    @Test
    fun `RiderWeightChanged with value below minimum shows error`() = runTest {
        coEvery {
            updateSettingsUseCase.updateRiderWeightKg("0.5")
        } returns Result.failure(InvalidWeightException(MIN_WEIGHT_KG.toDouble(), MAX_WEIGHT_KG.toDouble()))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.RiderWeightChanged("0.5"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("0.5", state.riderWeightKg)
        assertEquals(RiderWeightError(MIN_WEIGHT_KG, MAX_WEIGHT_KG), state.riderWeightError)
    }

    @Test
    fun `RiderWeightChanged with value above maximum shows error`() = runTest {
        coEvery {
            updateSettingsUseCase.updateRiderWeightKg("301")
        } returns Result.failure(InvalidWeightException(MIN_WEIGHT_KG.toDouble(), MAX_WEIGHT_KG.toDouble()))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.RiderWeightChanged("301"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("301", state.riderWeightKg)
        assertEquals(RiderWeightError(MIN_WEIGHT_KG, MAX_WEIGHT_KG), state.riderWeightError)
    }

    @Test
    fun `RiderWeightChanged with empty input shows error`() = runTest {
        coEvery {
            updateSettingsUseCase.updateRiderWeightKg("")
        } returns Result.failure(InvalidWeightException(MIN_WEIGHT_KG.toDouble(), MAX_WEIGHT_KG.toDouble()))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.RiderWeightChanged(""))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.riderWeightKg)
        assertEquals(RiderWeightError(MIN_WEIGHT_KG, MAX_WEIGHT_KG), state.riderWeightError)
    }

    @Test
    fun `rapid RiderWeightChanged only processes last value`() = runTest {
        coEvery { updateSettingsUseCase.updateRiderWeightKg("85") } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.RiderWeightChanged("8"))
        viewModel.onEvent(SettingsUiEvent.RiderWeightChanged("85"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("85", state.riderWeightKg)
        assertNull(state.riderWeightError)
        coVerify(exactly = 0) { updateSettingsUseCase.updateRiderWeightKg("8") }
        coVerify { updateSettingsUseCase.updateRiderWeightKg("85") }
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
