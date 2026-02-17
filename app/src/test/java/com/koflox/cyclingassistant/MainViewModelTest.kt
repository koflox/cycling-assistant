package com.koflox.cyclingassistant

import app.cash.turbine.test
import com.koflox.locale.domain.model.AppLanguage
import com.koflox.locale.domain.usecase.ObserveLocaleUseCase
import com.koflox.session.service.PendingSessionAction
import com.koflox.testing.coroutine.MainDispatcherRule
import com.koflox.theme.domain.model.AppTheme
import com.koflox.theme.domain.usecase.ObserveThemeUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    companion object {
        private const val TEST_ACTION = "com.koflox.session.TEST_ACTION"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeThemeUseCase: ObserveThemeUseCase = mockk()
    private val observeLocaleUseCase: ObserveLocaleUseCase = mockk()
    private val pendingSessionAction: PendingSessionAction = mockk(relaxed = true)

    private fun createViewModel() = MainViewModel(
        observeThemeUseCase = observeThemeUseCase,
        observeLocaleUseCase = observeLocaleUseCase,
        pendingSessionAction = pendingSessionAction,
    )

    @Test
    fun `initial state is Loading`() = runTest {
        every { observeThemeUseCase.observeTheme() } returns flowOf(AppTheme.SYSTEM)
        every { observeLocaleUseCase.observeLanguage() } returns flowOf(AppLanguage.ENGLISH)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is MainUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state becomes Ready when both flows emit`() = runTest {
        every { observeThemeUseCase.observeTheme() } returns flowOf(AppTheme.DARK)
        every { observeLocaleUseCase.observeLanguage() } returns flowOf(AppLanguage.RUSSIAN)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is MainUiState.Loading)
            val ready = awaitItem()
            assertTrue(ready is MainUiState.Ready)
            assertEquals(AppTheme.DARK, (ready as MainUiState.Ready).theme)
            assertEquals(AppLanguage.RUSSIAN, ready.language)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state updates when theme changes`() = runTest {
        val themeFlow = MutableStateFlow(AppTheme.LIGHT)
        every { observeThemeUseCase.observeTheme() } returns themeFlow
        every { observeLocaleUseCase.observeLanguage() } returns flowOf(AppLanguage.ENGLISH)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is MainUiState.Loading)

            val first = awaitItem() as MainUiState.Ready
            assertEquals(AppTheme.LIGHT, first.theme)

            themeFlow.value = AppTheme.DARK
            val second = awaitItem() as MainUiState.Ready
            assertEquals(AppTheme.DARK, second.theme)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state updates when language changes`() = runTest {
        val languageFlow = MutableStateFlow(AppLanguage.ENGLISH)
        every { observeThemeUseCase.observeTheme() } returns flowOf(AppTheme.SYSTEM)
        every { observeLocaleUseCase.observeLanguage() } returns languageFlow

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is MainUiState.Loading)

            val first = awaitItem() as MainUiState.Ready
            assertEquals(AppLanguage.ENGLISH, first.language)

            languageFlow.value = AppLanguage.JAPANESE
            val second = awaitItem() as MainUiState.Ready
            assertEquals(AppLanguage.JAPANESE, second.language)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handleIntent with null does nothing`() = runTest {
        every { observeThemeUseCase.observeTheme() } returns flowOf(AppTheme.SYSTEM)
        every { observeLocaleUseCase.observeLanguage() } returns flowOf(AppLanguage.ENGLISH)

        val viewModel = createViewModel()

        viewModel.handleIntent(null)

        verify(exactly = 0) { pendingSessionAction.handleIntentAction(any()) }
    }

    @Test
    fun `handleIntent with action delegates to pendingSessionAction`() = runTest {
        every { observeThemeUseCase.observeTheme() } returns flowOf(AppTheme.SYSTEM)
        every { observeLocaleUseCase.observeLanguage() } returns flowOf(AppLanguage.ENGLISH)

        val viewModel = createViewModel()

        viewModel.handleIntent(TEST_ACTION)

        verify(exactly = 1) { pendingSessionAction.handleIntentAction(TEST_ACTION) }
    }
}
