package com.koflox.destinationsession.bridge.impl.usecase

import app.cash.turbine.test
import com.koflox.session.domain.usecase.ActiveSessionUseCase
import com.koflox.session.domain.usecase.CreateSessionUseCase
import com.koflox.session.domain.usecase.NoActiveSessionException
import com.koflox.session.service.SessionServiceController
import com.koflox.session.testutil.createSession
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CyclingSessionUseCaseImplTest {

    companion object {
        private const val SESSION_ID = "session-123"
        private const val DESTINATION_ID = "dest-456"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activeSessionUseCase: ActiveSessionUseCase = mockk()
    private val createSessionUseCase: CreateSessionUseCase = mockk()
    private val sessionServiceController: SessionServiceController = mockk()
    private lateinit var useCase: CyclingSessionUseCaseImpl

    @Before
    fun setup() {
        useCase = CyclingSessionUseCaseImpl(
            activeSessionUseCase = activeSessionUseCase,
            createSessionUseCase = createSessionUseCase,
            sessionServiceController = sessionServiceController,
        )
    }

    @Suppress("UnusedFlow")
    @Test
    fun `observeHasActiveSession delegates to activeSessionUseCase`() = runTest {
        every { activeSessionUseCase.hasActiveSession() } returns flowOf(false)

        useCase.observeHasActiveSession()

        verify(exactly = 1) { activeSessionUseCase.hasActiveSession() }
    }

    @Test
    fun `observeHasActiveSession returns true when session exists`() = runTest {
        every { activeSessionUseCase.hasActiveSession() } returns flowOf(true)

        useCase.observeHasActiveSession().test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeHasActiveSession returns false when no session`() = runTest {
        every { activeSessionUseCase.hasActiveSession() } returns flowOf(false)

        useCase.observeHasActiveSession().test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeHasActiveSession emits updates`() = runTest {
        every { activeSessionUseCase.hasActiveSession() } returns flowOf(false, true, false)

        useCase.observeHasActiveSession().test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getActiveSessionDestination returns destination when session exists`() = runTest {
        val session = createSession(id = SESSION_ID, destinationId = DESTINATION_ID)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.getActiveSessionDestination()

        assertEquals(DESTINATION_ID, result?.id)
    }

    @Test
    fun `getActiveSessionDestination returns null when no active session`() = runTest {
        coEvery { activeSessionUseCase.getActiveSession() } throws NoActiveSessionException()

        val result = useCase.getActiveSessionDestination()

        assertNull(result)
    }

    @Test
    fun `getActiveSessionDestination handles NoActiveSessionException gracefully`() = runTest {
        coEvery { activeSessionUseCase.getActiveSession() } throws NoActiveSessionException()

        val result = useCase.getActiveSessionDestination()

        assertNull(result)
    }

    @Test
    fun `getActiveSessionDestination returns correct destination id`() = runTest {
        val customDestinationId = "custom-dest-id"
        val session = createSession(id = SESSION_ID, destinationId = customDestinationId)
        coEvery { activeSessionUseCase.getActiveSession() } returns session

        val result = useCase.getActiveSessionDestination()

        assertEquals(customDestinationId, result?.id)
    }
}
