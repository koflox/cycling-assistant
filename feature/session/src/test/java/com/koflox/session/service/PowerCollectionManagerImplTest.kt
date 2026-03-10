package com.koflox.session.service

import com.koflox.connectionsession.bridge.model.PowerReadingData
import com.koflox.connectionsession.bridge.model.SessionPowerDevice
import com.koflox.connectionsession.bridge.usecase.PowerConnectionException
import com.koflox.connectionsession.bridge.usecase.SessionPowerMeterUseCase
import com.koflox.session.domain.usecase.UpdateSessionPowerUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PowerCollectionManagerImplTest {

    companion object {
        private const val MAC_ADDRESS = "AA:BB:CC:DD:EE:FF"
        private const val DEVICE_NAME = "PowerMeter X"
        private const val POWER_WATTS = 200
        private const val TIMESTAMP_MS = 1000L
    }

    private val testDispatcher = StandardTestDispatcher()
    private val sessionPowerMeterUseCase: SessionPowerMeterUseCase = mockk()
    private val updateSessionPowerUseCase: UpdateSessionPowerUseCase = mockk(relaxed = true)
    private val stateHolder = PowerConnectionStateHolderImpl()

    private lateinit var manager: PowerCollectionManagerImpl

    @Before
    fun setup() {
        justRun { sessionPowerMeterUseCase.disconnect() }
        manager = PowerCollectionManagerImpl(
            sessionPowerMeterUseCase = sessionPowerMeterUseCase,
            updateSessionPowerUseCase = updateSessionPowerUseCase,
            powerConnectionStatePublisher = stateHolder,
        )
    }

    @Test
    fun `start with no device does not publish state`() = runManagerTest {
        coEvery { sessionPowerMeterUseCase.getSessionPowerDevice() } returns null

        manager.start(this)
        advanceUntilIdle()

        assertNull(stateHolder.deviceConnectionInfo.value)
    }

    @Test
    fun `start publishes Connecting state`() = runManagerTest {
        coEvery { sessionPowerMeterUseCase.getSessionPowerDevice() } returns createDevice()
        every { sessionPowerMeterUseCase.observePowerReadings(MAC_ADDRESS) } returns MutableSharedFlow()

        manager.start(this)
        advanceTimeBy(1)

        val info = stateHolder.deviceConnectionInfo.value
        assertEquals(DEVICE_NAME, info?.deviceName)
        assertEquals(PowerConnectionState.Connecting, info?.state)
    }

    @Test
    fun `receiving power data publishes Connected state and updates session`() = runManagerTest {
        val readingsFlow = MutableSharedFlow<PowerReadingData>()
        coEvery { sessionPowerMeterUseCase.getSessionPowerDevice() } returns createDevice()
        every { sessionPowerMeterUseCase.observePowerReadings(MAC_ADDRESS) } returns readingsFlow

        manager.start(this)
        advanceTimeBy(1)

        readingsFlow.emit(PowerReadingData(TIMESTAMP_MS, POWER_WATTS, null))
        advanceTimeBy(1)

        val info = stateHolder.deviceConnectionInfo.value
        assertEquals(PowerConnectionState.Connected(POWER_WATTS), info?.state)
        coVerify { updateSessionPowerUseCase.update(powerWatts = POWER_WATTS, timestampMs = TIMESTAMP_MS) }
    }

    @Test
    fun `connection error triggers Reconnecting countdown`() = runManagerTest {
        coEvery { sessionPowerMeterUseCase.getSessionPowerDevice() } returns createDevice()
        every { sessionPowerMeterUseCase.observePowerReadings(MAC_ADDRESS) } returns flow {
            throw PowerConnectionException(RuntimeException("BLE error"))
        }

        manager.start(this)
        advanceTimeBy(1)

        val info = stateHolder.deviceConnectionInfo.value
        assertEquals(
            PowerConnectionState.Reconnecting(PowerCollectionManagerImpl.RETRY_INITIAL_DELAY),
            info?.state,
        )
    }

    @Test
    fun `countdown ticks down every second`() = runManagerTest {
        coEvery { sessionPowerMeterUseCase.getSessionPowerDevice() } returns createDevice()
        every { sessionPowerMeterUseCase.observePowerReadings(MAC_ADDRESS) } returns flow {
            throw PowerConnectionException(RuntimeException("BLE error"))
        }

        manager.start(this)
        advanceTimeBy(1)

        // Initial retry delay is 2s, countdown should show 2s then 1s
        assertEquals(PowerConnectionState.Reconnecting(2.seconds), stateHolder.deviceConnectionInfo.value?.state)

        advanceTimeBy(1000)
        assertEquals(PowerConnectionState.Reconnecting(1.seconds), stateHolder.deviceConnectionInfo.value?.state)
    }

    @Test
    fun `retry delay doubles with exponential backoff`() = runManagerTest {
        var callCount = 0
        coEvery { sessionPowerMeterUseCase.getSessionPowerDevice() } returns createDevice()
        every { sessionPowerMeterUseCase.observePowerReadings(MAC_ADDRESS) } returns flow {
            callCount++
            throw PowerConnectionException(RuntimeException("BLE error"))
        }

        manager.start(this)

        // First failure: 2s countdown
        advanceTimeBy(1)
        assertEquals(PowerConnectionState.Reconnecting(2.seconds), stateHolder.deviceConnectionInfo.value?.state)

        // Wait past first retry (2s) + brief processing
        advanceTimeBy(2000)

        // Second failure: 4s countdown
        advanceTimeBy(1)
        assertEquals(PowerConnectionState.Reconnecting(4.seconds), stateHolder.deviceConnectionInfo.value?.state)
    }

    @Test
    fun `successful reading resets retry delay`() = runManagerTest {
        var failFirst = true
        val readingsFlow = MutableSharedFlow<PowerReadingData>()
        coEvery { sessionPowerMeterUseCase.getSessionPowerDevice() } returns createDevice()
        every { sessionPowerMeterUseCase.observePowerReadings(MAC_ADDRESS) } answers {
            if (failFirst) {
                failFirst = false
                flow { throw PowerConnectionException(RuntimeException("BLE error")) }
            } else {
                readingsFlow
            }
        }

        manager.start(this)

        // First failure + 2s countdown + reconnect
        advanceTimeBy(2001)

        // Now connected, emit a reading
        readingsFlow.emit(PowerReadingData(TIMESTAMP_MS, POWER_WATTS, null))
        advanceTimeBy(1)

        assertEquals(PowerConnectionState.Connected(POWER_WATTS), stateHolder.deviceConnectionInfo.value?.state)
    }

    @Test
    fun `stop cancels collection and clears state`() = runManagerTest {
        coEvery { sessionPowerMeterUseCase.getSessionPowerDevice() } returns createDevice()
        every { sessionPowerMeterUseCase.observePowerReadings(MAC_ADDRESS) } returns MutableSharedFlow()

        manager.start(this)
        advanceTimeBy(1)

        manager.stop()

        assertNull(stateHolder.deviceConnectionInfo.value)
        verify { sessionPowerMeterUseCase.disconnect() }
    }

    @Test
    fun `start is idempotent when already active`() = runManagerTest {
        coEvery { sessionPowerMeterUseCase.getSessionPowerDevice() } returns createDevice()
        every { sessionPowerMeterUseCase.observePowerReadings(MAC_ADDRESS) } returns MutableSharedFlow()

        manager.start(this)
        manager.start(this)
        advanceTimeBy(1)

        coVerify(exactly = 1) { sessionPowerMeterUseCase.getSessionPowerDevice() }
    }

    private fun runManagerTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        try {
            block()
        } finally {
            manager.stop()
        }
    }

    private fun createDevice() = SessionPowerDevice(macAddress = MAC_ADDRESS, name = DEVICE_NAME)
}
