package com.koflox.strava.impl.presentation.connect

import app.cash.turbine.test
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.impl.domain.usecase.StravaAuthUseCase
import com.koflox.strava.impl.oauth.StravaAuthEvents
import com.koflox.strava.impl.oauth.StravaAuthHint
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StravaConnectViewModelTest {

    companion object {
        private const val ATHLETE_ID = 12345L
        private const val ATHLETE_NAME = "John Doe"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authUseCase: StravaAuthUseCase = mockk()
    private val authEvents: StravaAuthEvents = mockk()
    private val authStateFlow = MutableStateFlow<StravaAuthState>(StravaAuthState.LoggedOut)
    private val hintFlow = MutableStateFlow<StravaAuthHint?>(null)

    private fun createViewModel(): StravaConnectViewModel {
        every { authUseCase.observeAuthState() } returns authStateFlow
        every { authEvents.hint } returns hintFlow
        justRun { authEvents.consume() }
        return StravaConnectViewModel(
            authUseCase = authUseCase,
            authEvents = authEvents,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `observe auth state surfaces LoggedOut as Content`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(StravaConnectUiState.Loading, awaitItem())
            assertEquals(
                StravaConnectUiState.Content(authState = StravaAuthState.LoggedOut),
                awaitItem(),
            )
        }
    }

    @Test
    fun `observe auth state transitions to LoggedIn`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)
            authStateFlow.value = StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME)
            assertEquals(
                StravaConnectUiState.Content(authState = StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME)),
                awaitItem(),
            )
        }
    }

    @Test
    fun `MissingRequiredScopes hint is reflected in Content state`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)
            hintFlow.value = StravaAuthHint.MissingRequiredScopes
            assertEquals(
                StravaConnectUiState.Content(
                    authState = StravaAuthState.LoggedOut,
                    hint = StravaAuthHint.MissingRequiredScopes,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `HintDismissed delegates to authEvents consume`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(StravaConnectUiEvent.HintDismissed)
        advanceUntilIdle()

        verify { authEvents.consume() }
    }

    @Test
    fun `ConnectClicked emits LaunchOAuthIntent navigation`() = runTest {
        val viewModel = createViewModel()

        viewModel.navigation.test {
            viewModel.onEvent(StravaConnectUiEvent.ConnectClicked)
            assertEquals(StravaConnectNavigation.LaunchOAuthIntent, awaitItem())
        }
    }

    @Test
    fun `LogoutClicked shows confirm overlay`() = runTest {
        authStateFlow.value = StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)
            viewModel.onEvent(StravaConnectUiEvent.LogoutClicked)
            assertEquals(
                StravaConnectUiState.Content(
                    authState = StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME),
                    overlay = StravaConnectUiState.Content.Overlay.LogoutConfirm,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `LogoutDismissed clears overlay`() = runTest {
        authStateFlow.value = StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)
            viewModel.onEvent(StravaConnectUiEvent.LogoutClicked)
            skipItems(1)
            viewModel.onEvent(StravaConnectUiEvent.LogoutDismissed)
            assertEquals(
                StravaConnectUiState.Content(authState = StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME)),
                awaitItem(),
            )
        }
    }

    @Test
    fun `LogoutConfirmed dismisses overlay and calls logout`() = runTest {
        authStateFlow.value = StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME)
        coEvery { authUseCase.logout() } returns Result.success(Unit)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(2)
            viewModel.onEvent(StravaConnectUiEvent.LogoutClicked)
            skipItems(1)
            viewModel.onEvent(StravaConnectUiEvent.LogoutConfirmed)
            assertEquals(
                StravaConnectUiState.Content(authState = StravaAuthState.LoggedIn(ATHLETE_ID, ATHLETE_NAME)),
                awaitItem(),
            )
        }
        coVerify { authUseCase.logout() }
    }

    @Test
    fun `LogoutConfirmed does nothing when not logged in`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(StravaConnectUiEvent.LogoutConfirmed)

        coVerify(exactly = 0) { authUseCase.logout() }
    }
}
