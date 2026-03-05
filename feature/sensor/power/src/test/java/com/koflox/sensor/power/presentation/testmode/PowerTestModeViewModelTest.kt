package com.koflox.sensor.power.presentation.testmode

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.koflox.ble.state.BluetoothStateMonitor
import com.koflox.sensor.power.domain.model.PowerReading
import com.koflox.sensor.power.domain.usecase.ObservePowerDataUseCase
import com.koflox.sensor.power.navigation.MAC_ADDRESS_ARG
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PowerTestModeViewModelTest {

    companion object {
        private const val MAC_ADDRESS = "AA:BB:CC:DD:EE:FF"
        private const val POWER_WATTS = 200
        private const val CADENCE_RPM = 60f
        private const val TIMESTAMP_MS = 1000L
        private const val EXPECTED_SENSOR_STATS_COUNT = 7
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observePowerDataUseCase: ObservePowerDataUseCase = mockk()
    private val bluetoothStateMonitor: BluetoothStateMonitor = mockk()
    private val savedStateHandle = SavedStateHandle(mapOf(MAC_ADDRESS_ARG to MAC_ADDRESS))
    private lateinit var viewModel: PowerTestModeViewModel

    @Before
    fun setup() {
        justRun { observePowerDataUseCase.disconnect() }
        every { bluetoothStateMonitor.observeBluetoothEnabled() } returns flowOf(true)
    }

    private fun createViewModel() = PowerTestModeViewModel(
        observePowerDataUseCase = observePowerDataUseCase,
        bluetoothStateMonitor = bluetoothStateMonitor,
        dispatcherDefault = mainDispatcherRule.testDispatcher,
        savedStateHandle = savedStateHandle,
    )

    private fun createReading(
        timestampMs: Long = TIMESTAMP_MS,
        powerWatts: Int = POWER_WATTS,
        cadenceRpm: Float? = CADENCE_RPM,
        pedalPowerBalancePercent: Float? = null,
        accumulatedTorqueNm: Float? = null,
        wheelSpeedKmh: Float? = null,
        accumulatedEnergyKj: Int? = null,
    ) = PowerReading(
        timestampMs = timestampMs,
        powerWatts = powerWatts,
        cadenceRpm = cadenceRpm,
        pedalPowerBalancePercent = pedalPowerBalancePercent,
        accumulatedTorqueNm = accumulatedTorqueNm,
        wheelSpeedKmh = wheelSpeedKmh,
        accumulatedEnergyKj = accumulatedEnergyKj,
    )

    @Test
    fun `initial state is Connecting`() = runTest {
        every { observePowerDataUseCase.observePowerData(MAC_ADDRESS) } returns flowOf()
        viewModel = createViewModel()
        viewModel.uiState.test {
            assertTrue(awaitItem() is PowerTestModeUiState.Connecting)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `power reading updates to Connected state`() = runTest {
        val reading = createReading()
        every { observePowerDataUseCase.observePowerData(MAC_ADDRESS) } returns flowOf(reading)
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Connecting
            val connected = awaitItem() as PowerTestModeUiState.Connected
            assertEquals(POWER_WATTS, connected.currentPowerWatts)
            assertEquals(CADENCE_RPM, connected.currentCadenceRpm)
            assertEquals(EXPECTED_SENSOR_STATS_COUNT, connected.sensorStats.size)
            assertEquals(1, connected.recentReadings.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `flow completion transitions to Disconnected`() = runTest {
        every { observePowerDataUseCase.observePowerData(MAC_ADDRESS) } returns flowOf()
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Connecting
            val disconnected = awaitItem() as PowerTestModeUiState.Disconnected
            assertTrue(disconnected.message is com.koflox.designsystem.text.UiText.Resource)
        }
    }

    @Test
    fun `Disconnect event stops collection`() = runTest {
        val powerFlow = MutableSharedFlow<PowerReading>()
        every { observePowerDataUseCase.observePowerData(MAC_ADDRESS) } returns powerFlow
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Connecting
            viewModel.onEvent(PowerTestModeUiEvent.Disconnect)
            val state = awaitItem() as PowerTestModeUiState.Disconnected
            assertTrue(state.message is com.koflox.designsystem.text.UiText.Resource)
            verify { observePowerDataUseCase.disconnect() }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
