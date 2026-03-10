package com.koflox.sensor.power.presentation.testmode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.ble.state.BluetoothStateMonitor
import com.koflox.designsystem.text.UiText
import com.koflox.di.DefaultDispatcher
import com.koflox.sensor.power.R
import com.koflox.sensor.power.domain.model.PowerReading
import com.koflox.sensor.power.domain.usecase.ObservePowerDataUseCase
import com.koflox.sensor.power.domain.usecase.PowerMeterConnectionException
import com.koflox.sensor.power.navigation.MAC_ADDRESS_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PowerTestModeViewModel @Inject internal constructor(
    private val observePowerDataUseCase: ObservePowerDataUseCase,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val MAX_READINGS = 60
        private const val MS_TO_SECONDS = 1000.0
        private const val WATTS_SECONDS_TO_KJ = 1000.0
        private const val STAT_DECIMAL_FORMAT = "%.1f"
        private const val UNAVAILABLE_VALUE = "---"
    }

    private val macAddress: String = checkNotNull(savedStateHandle[MAC_ADDRESS_ARG])

    private val _uiState = MutableStateFlow<PowerTestModeUiState>(PowerTestModeUiState.Connecting)
    val uiState: StateFlow<PowerTestModeUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<PowerTestModeNavigation>()
    val navigation = _navigation.receiveAsFlow()

    private val readings = mutableListOf<PowerReadingUiModel>()
    private var totalPower = 0L
    private var maxPower = 0
    private var readingCount = 0
    private var totalEnergyKj = 0.0
    private var lastReadingTimestampMs = 0L
    private var connectionJob: Job? = null

    init {
        initialize()
    }

    private fun initialize() {
        connectIfBluetoothEnabled()
    }

    fun onEvent(event: PowerTestModeUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is PowerTestModeUiEvent.Disconnect -> handleDisconnect()
                is PowerTestModeUiEvent.Reconnect -> connectIfBluetoothEnabled()
                is PowerTestModeUiEvent.BluetoothDisabledDismissed -> updateOverlay(null)
            }
        }
    }

    private fun connectIfBluetoothEnabled() {
        viewModelScope.launch(dispatcherDefault) {
            val isEnabled = bluetoothStateMonitor.observeBluetoothEnabled().first()
            if (isEnabled) {
                connectToDevice()
            } else {
                updateOverlay(PowerTestModeOverlay.BluetoothDisabled)
            }
        }
    }

    private fun connectToDevice() {
        connectionJob?.cancel()
        _uiState.value = PowerTestModeUiState.Connecting
        resetStats()
        connectionJob = viewModelScope.launch(dispatcherDefault) {
            try {
                observePowerDataUseCase.observePowerData(macAddress).collect { reading ->
                    readingCount++
                    totalPower += reading.powerWatts
                    if (reading.powerWatts > maxPower) maxPower = reading.powerWatts
                    if (lastReadingTimestampMs > 0) {
                        val deltaSec = (reading.timestampMs - lastReadingTimestampMs) / MS_TO_SECONDS
                        totalEnergyKj += reading.powerWatts * deltaSec / WATTS_SECONDS_TO_KJ
                    }
                    lastReadingTimestampMs = reading.timestampMs
                    val uiReading = PowerReadingUiModel(
                        timestampMs = reading.timestampMs,
                        powerWatts = reading.powerWatts,
                    )
                    readings.add(uiReading)
                    if (readings.size > MAX_READINGS) readings.removeAt(0)
                    _uiState.value = PowerTestModeUiState.Connected(
                        currentPowerWatts = reading.powerWatts,
                        currentCadenceRpm = reading.cadenceRpm,
                        sensorStats = buildSensorStats(reading),
                        recentReadings = readings.toList(),
                    )
                }
                _uiState.value = PowerTestModeUiState.Disconnected(
                    message = UiText.Resource(R.string.power_test_disconnected),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: PowerMeterConnectionException) {
                _uiState.value = PowerTestModeUiState.Error(
                    message = UiText.Resource(R.string.power_test_connection_failed),
                )
            }
        }
    }

    private fun buildSensorStats(reading: PowerReading): List<PowerTestStatItem> {
        val unavailable = UNAVAILABLE_VALUE
        val averagePower = if (readingCount > 0) (totalPower / readingCount).toInt() else 0
        return listOf(
            PowerTestStatItem(
                label = UiText.Resource(R.string.power_test_pedal_balance),
                value = reading.pedalPowerBalancePercent?.let { STAT_DECIMAL_FORMAT.format(it) } ?: unavailable,
                unit = UiText.Resource(R.string.power_test_unit_percent),
            ),
            PowerTestStatItem(
                label = UiText.Resource(R.string.power_test_torque),
                value = reading.accumulatedTorqueNm?.let { STAT_DECIMAL_FORMAT.format(it) } ?: unavailable,
                unit = UiText.Resource(R.string.power_test_unit_nm),
            ),
            PowerTestStatItem(
                label = UiText.Resource(R.string.power_test_wheel_speed),
                value = reading.wheelSpeedKmh?.let { STAT_DECIMAL_FORMAT.format(it) } ?: unavailable,
                unit = UiText.Resource(R.string.power_test_unit_kmh),
            ),
            PowerTestStatItem(
                label = UiText.Resource(R.string.power_test_energy),
                value = reading.accumulatedEnergyKj?.toString() ?: unavailable,
                unit = UiText.Resource(R.string.power_test_unit_kj),
            ),
            PowerTestStatItem(
                label = UiText.Resource(R.string.power_test_average_power),
                value = "$averagePower",
                unit = UiText.Resource(R.string.power_test_unit_watts),
            ),
            PowerTestStatItem(
                label = UiText.Resource(R.string.power_test_max_power),
                value = "$maxPower",
                unit = UiText.Resource(R.string.power_test_unit_watts),
            ),
            PowerTestStatItem(
                label = UiText.Resource(R.string.power_test_calories),
                value = "${totalEnergyKj.toInt()}",
                unit = UiText.Resource(R.string.power_test_unit_kcal),
            ),
        )
    }

    private fun handleDisconnect() {
        connectionJob?.cancel()
        observePowerDataUseCase.disconnect()
        _uiState.value = PowerTestModeUiState.Disconnected(
            message = UiText.Resource(R.string.power_test_disconnected),
        )
    }

    private fun updateOverlay(overlay: PowerTestModeOverlay?) {
        when (val current = _uiState.value) {
            is PowerTestModeUiState.Disconnected -> _uiState.value = current.copy(overlay = overlay)
            is PowerTestModeUiState.Error -> _uiState.value = current.copy(overlay = overlay)
            is PowerTestModeUiState.Connecting,
            is PowerTestModeUiState.Connected,
            -> Unit
        }
    }

    private fun resetStats() {
        readings.clear()
        totalPower = 0L
        maxPower = 0
        readingCount = 0
        totalEnergyKj = 0.0
        lastReadingTimestampMs = 0L
    }

    override fun onCleared() {
        super.onCleared()
        connectionJob?.cancel()
        observePowerDataUseCase.disconnect()
    }
}
