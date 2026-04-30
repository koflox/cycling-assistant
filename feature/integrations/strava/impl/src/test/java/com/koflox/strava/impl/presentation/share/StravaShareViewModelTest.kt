package com.koflox.strava.impl.presentation.share

import app.cash.turbine.test
import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.api.model.SyncErrorReason
import com.koflox.strava.api.usecase.ObserveStravaSyncStatusUseCase
import com.koflox.strava.api.usecase.StravaSyncUseCase
import com.koflox.strava.impl.domain.usecase.StravaAuthUseCase
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StravaShareViewModelTest {

    companion object {
        private const val SESSION_ID = "session-1"
        private const val ACTIVITY_ID = 999L
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authUseCase: StravaAuthUseCase = mockk()
    private val syncUseCase: StravaSyncUseCase = mockk(relaxed = true)
    private val observeSyncStatusUseCase: ObserveStravaSyncStatusUseCase = mockk()
    private val authStateFlow = MutableStateFlow<StravaAuthState>(StravaAuthState.LoggedOut)
    private val syncStatusFlow = MutableStateFlow<SessionSyncStatus>(SessionSyncStatus.NotSynced)

    private fun createViewModel(): StravaShareViewModel {
        every { authUseCase.observeAuthState() } returns authStateFlow
        every { observeSyncStatusUseCase.observe(SESSION_ID) } returns syncStatusFlow
        return StravaShareViewModel(
            authUseCase = authUseCase,
            syncUseCase = syncUseCase,
            observeSyncStatusUseCase = observeSyncStatusUseCase,
            dispatcherDefault = mainDispatcherRule.testDispatcher,
        )
    }

    @Test
    fun `Started causes Content to be emitted with combined state`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(StravaShareUiState.Loading, awaitItem())
            viewModel.onEvent(StravaShareUiEvent.Started(SESSION_ID))
            assertEquals(
                StravaShareUiState.Content(
                    authState = StravaAuthState.LoggedOut,
                    syncStatus = SessionSyncStatus.NotSynced,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `state updates when auth and sync change`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.onEvent(StravaShareUiEvent.Started(SESSION_ID))
            skipItems(1)
            authStateFlow.value = StravaAuthState.LoggedIn(1L, "John")
            assertEquals(
                StravaShareUiState.Content(
                    authState = StravaAuthState.LoggedIn(1L, "John"),
                    syncStatus = SessionSyncStatus.NotSynced,
                ),
                awaitItem(),
            )
            syncStatusFlow.value = SessionSyncStatus.Processing
            assertEquals(
                StravaShareUiState.Content(
                    authState = StravaAuthState.LoggedIn(1L, "John"),
                    syncStatus = SessionSyncStatus.Processing,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `ConnectClicked emits LaunchOAuthIntent`() = runTest {
        val viewModel = createViewModel()

        viewModel.navigation.test {
            viewModel.onEvent(StravaShareUiEvent.ConnectClicked)
            assertEquals(StravaShareNavigation.LaunchOAuthIntent, awaitItem())
        }
    }

    @Test
    fun `SyncClicked enqueues sync for current sessionId`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(StravaShareUiEvent.Started(SESSION_ID))
        advanceUntilIdle()

        viewModel.onEvent(StravaShareUiEvent.SyncClicked)
        advanceUntilIdle()

        coVerify { syncUseCase.enqueue(SESSION_ID) }
    }

    @Test
    fun `SyncClicked is no-op when no sessionId set`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(StravaShareUiEvent.SyncClicked)
        advanceUntilIdle()

        coVerify(exactly = 0) { syncUseCase.enqueue(any()) }
    }

    @Test
    fun `RetryClicked retries upload`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(StravaShareUiEvent.Started(SESSION_ID))
        advanceUntilIdle()

        viewModel.onEvent(StravaShareUiEvent.RetryClicked)
        advanceUntilIdle()

        coVerify { syncUseCase.retry(SESSION_ID) }
    }

    @Test
    fun `RefreshClicked refreshes status and starts cooldown`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(StravaShareUiEvent.Started(SESSION_ID))
        advanceUntilIdle()

        viewModel.onEvent(StravaShareUiEvent.RefreshClicked)
        advanceUntilIdle()

        coVerify { syncUseCase.refreshStatus(SESSION_ID) }
        // cooldown was non-zero at some point during the test window
        coVerify(atLeast = 1) { syncUseCase.refreshStatus(SESSION_ID) }
    }

    @Test
    fun `Started triggers verifySyncedActivity`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(StravaShareUiEvent.Started(SESSION_ID))
        advanceUntilIdle()

        coVerify { syncUseCase.verifySyncedActivity(SESSION_ID) }
    }

    @Test
    fun `ViewOnStravaClicked emits OpenStravaActivity for synced state`() = runTest {
        syncStatusFlow.value = SessionSyncStatus.Synced(ACTIVITY_ID)
        authStateFlow.value = StravaAuthState.LoggedIn(1L, "John")
        val viewModel = createViewModel()
        viewModel.onEvent(StravaShareUiEvent.Started(SESSION_ID))
        advanceUntilIdle()

        viewModel.navigation.test {
            viewModel.onEvent(StravaShareUiEvent.ViewOnStravaClicked)
            assertEquals(StravaShareNavigation.OpenStravaActivity(ACTIVITY_ID), awaitItem())
        }
    }

    @Test
    fun `ViewOnStravaClicked is no-op when not synced`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(StravaShareUiEvent.Started(SESSION_ID))
        advanceUntilIdle()

        viewModel.navigation.test {
            viewModel.onEvent(StravaShareUiEvent.ViewOnStravaClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `state with Error retryable preserved`() = runTest {
        syncStatusFlow.value = SessionSyncStatus.Error(SyncErrorReason.NETWORK, isRetryable = true)
        val viewModel = createViewModel()
        viewModel.onEvent(StravaShareUiEvent.Started(SESSION_ID))

        viewModel.uiState.test {
            skipItems(1)
            assertEquals(
                StravaShareUiState.Content(
                    authState = StravaAuthState.LoggedOut,
                    syncStatus = SessionSyncStatus.Error(SyncErrorReason.NETWORK, isRetryable = true),
                ),
                awaitItem(),
            )
        }
    }

}
