package com.koflox.session.service

import com.koflox.location.model.Location
import com.koflox.location.usecase.CheckLocationEnabledUseCase
import com.koflox.location.usecase.ObserveUserLocationUseCase
import com.koflox.session.domain.usecase.UpdateSessionLocationUseCase
import com.koflox.session.domain.usecase.UpdateSessionStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationCollectionManagerImplTest {

    companion object {
        private const val TEST_LATITUDE = 50.0
        private const val TEST_LONGITUDE = 14.0
        private const val TEST_TIMESTAMP_MS = 1000L
    }

    private val testDispatcher = StandardTestDispatcher()
    private val observeUserLocationUseCase: ObserveUserLocationUseCase = mockk()
    private val updateSessionLocationUseCase: UpdateSessionLocationUseCase = mockk()
    private val checkLocationEnabledUseCase: CheckLocationEnabledUseCase = mockk()
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase = mockk()

    private val locationFlow = MutableSharedFlow<Location>()
    private val locationEnabledFlow = MutableStateFlow(true)

    private lateinit var manager: LocationCollectionManagerImpl

    @Before
    fun setup() {
        every { observeUserLocationUseCase.observe(any(), any(), any()) } returns locationFlow
        every { checkLocationEnabledUseCase.observeLocationEnabled() } returns locationEnabledFlow
        coEvery { updateSessionLocationUseCase.update(any(), any()) } returns Unit
        coEvery { updateSessionStatusUseCase.pause() } returns Result.success(Unit)
        manager = LocationCollectionManagerImpl(
            observeUserLocationUseCase = observeUserLocationUseCase,
            updateSessionLocationUseCase = updateSessionLocationUseCase,
            checkLocationEnabledUseCase = checkLocationEnabledUseCase,
            updateSessionStatusUseCase = updateSessionStatusUseCase,
            currentTimeProvider = { TEST_TIMESTAMP_MS },
        )
    }

    @Test
    fun `start observes location updates`() = runManagerTest {
        manager.start(this)
        advanceTimeBy(1)

        every { observeUserLocationUseCase.observe(any(), any(), any()) }
    }

    @Test
    fun `location update calls updateSessionLocationUseCase`() = runManagerTest {
        manager.start(this)
        advanceTimeBy(1)

        locationFlow.emit(Location(latitude = TEST_LATITUDE, longitude = TEST_LONGITUDE))
        advanceTimeBy(1)

        coVerify { updateSessionLocationUseCase.update(any(), TEST_TIMESTAMP_MS) }
    }

    @Test
    fun `processes multiple location emissions`() = runManagerTest {
        manager.start(this)
        advanceTimeBy(1)

        repeat(3) {
            locationFlow.emit(Location(latitude = TEST_LATITUDE, longitude = TEST_LONGITUDE))
            advanceTimeBy(1)
        }

        coVerify(exactly = 3) { updateSessionLocationUseCase.update(any(), any()) }
    }

    @Test
    fun `start observes location enabled state`() = runManagerTest {
        manager.start(this)
        advanceTimeBy(1)

        every { checkLocationEnabledUseCase.observeLocationEnabled() }
    }

    @Test
    fun `location disabled triggers session pause`() = runManagerTest {
        manager.start(this)
        advanceTimeBy(1)

        locationEnabledFlow.value = false
        advanceTimeBy(1)

        coVerify { updateSessionStatusUseCase.pause() }
    }

    @Test
    fun `location enabled does not trigger pause`() = runManagerTest {
        manager.start(this)
        advanceTimeBy(1)

        locationEnabledFlow.value = true
        advanceTimeBy(1)

        coVerify(exactly = 0) { updateSessionStatusUseCase.pause() }
    }

    @Test
    fun `stop prevents further location processing`() = runManagerTest {
        manager.start(this)
        advanceTimeBy(1)

        locationFlow.emit(Location(latitude = TEST_LATITUDE, longitude = TEST_LONGITUDE))
        advanceTimeBy(1)

        manager.stop()

        coVerify(exactly = 1) { updateSessionLocationUseCase.update(any(), any()) }
    }

    @Test
    fun `start is idempotent when already active`() = runManagerTest {
        manager.start(this)
        manager.start(this)
        advanceTimeBy(1)

        locationFlow.emit(Location(latitude = TEST_LATITUDE, longitude = TEST_LONGITUDE))
        advanceTimeBy(1)

        coVerify(exactly = 1) { updateSessionLocationUseCase.update(any(), any()) }
    }

    private fun runManagerTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        try {
            block()
        } finally {
            manager.stop()
        }
    }
}
