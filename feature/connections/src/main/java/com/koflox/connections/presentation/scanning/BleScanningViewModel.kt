package com.koflox.connections.presentation.scanning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.ble.permission.BlePermissionChecker
import com.koflox.ble.scanner.BleScanner
import com.koflox.connections.R
import com.koflox.connections.domain.model.DeviceType
import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCase
import com.koflox.connections.domain.usecase.SavePairedDeviceUseCase
import com.koflox.designsystem.text.UiText
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.sensorprotocol.power.CyclingPowerConstants
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

internal class BleScanningViewModel(
    private val bleScanner: BleScanner,
    private val blePermissionChecker: BlePermissionChecker,
    private val observePairedDevicesUseCase: ObservePairedDevicesUseCase,
    private val savePairedDeviceUseCase: SavePairedDeviceUseCase,
    private val errorMessageMapper: ErrorMessageMapper,
    private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    companion object {
        private val SCAN_TIMEOUT = 30.seconds
    }

    private val _uiState = MutableStateFlow<BleScanningUiState>(BleScanningUiState.Scanning(emptyList()))
    val uiState: StateFlow<BleScanningUiState> = _uiState.asStateFlow()

    private val _isDismissed = MutableStateFlow(false)
    val isDismissed: StateFlow<Boolean> = _isDismissed.asStateFlow()

    private var scanJob: Job? = null

    init {
        initialize()
    }

    private fun initialize() {
        if (blePermissionChecker.hasPermissions()) {
            startScanning()
        } else {
            _uiState.value = BleScanningUiState.PermissionRequired
        }
    }

    fun onEvent(event: BleScanningUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is BleScanningUiEvent.StartScan -> startScanning()
                is BleScanningUiEvent.StopScan -> stopScanning()
                is BleScanningUiEvent.DeviceSelected -> handleDeviceSelected(event.macAddress, event.name)
                is BleScanningUiEvent.PermissionGranted -> startScanning()
                is BleScanningUiEvent.PermissionDenied -> _isDismissed.value = true
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun startScanning() {
        scanJob?.cancel()
        _uiState.value = BleScanningUiState.Scanning(emptyList())
        scanJob = viewModelScope.launch(dispatcherDefault) {
            try {
                val pairedAddresses = observePairedDevicesUseCase.observeAll()
                    .first()
                    .map { it.macAddress }
                    .toSet()
                bleScanner.scan(
                    serviceUuids = listOf(CyclingPowerConstants.SERVICE_UUID),
                    timeoutMs = SCAN_TIMEOUT.inWholeMilliseconds,
                ).collect { device ->
                    val currentState = _uiState.value
                    if (currentState is BleScanningUiState.Scanning) {
                        val updatedDevices = currentState.devices + ScannedDeviceUiModel(
                            macAddress = device.address,
                            name = device.name,
                            isAlreadyPaired = device.address in pairedAddresses,
                        )
                        _uiState.value = BleScanningUiState.Scanning(updatedDevices)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.value = BleScanningUiState.Error(
                    UiText.Resource(R.string.connections_error_scan_failed),
                )
            }
        }
    }

    private fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
    }

    private suspend fun handleDeviceSelected(macAddress: String, name: String) {
        stopScanning()
        savePairedDeviceUseCase.save(macAddress, name, DeviceType.POWER_METER)
            .onSuccess { _isDismissed.value = true }
            .onFailure { error ->
                _uiState.value = BleScanningUiState.Error(errorMessageMapper.map(error))
            }
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }
}
