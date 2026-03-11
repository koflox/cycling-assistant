package com.koflox.connections.presentation.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.ble.state.BluetoothStateMonitor
import com.koflox.connections.domain.usecase.DeletePairedDeviceUseCase
import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCase
import com.koflox.connections.domain.usecase.UpdateDeviceSessionUsageUseCase
import com.koflox.di.ConnectionsErrorMapper
import com.koflox.di.DefaultDispatcher
import com.koflox.error.mapper.ErrorMessageMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DeviceListViewModel @Inject constructor(
    private val observePairedDevicesUseCase: ObservePairedDevicesUseCase,
    private val deletePairedDeviceUseCase: DeletePairedDeviceUseCase,
    private val updateDeviceSessionUsageUseCase: UpdateDeviceSessionUsageUseCase,
    private val bluetoothStateMonitor: BluetoothStateMonitor,
    @param:ConnectionsErrorMapper private val errorMessageMapper: ErrorMessageMapper,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeviceListUiState>(DeviceListUiState.Loading)
    val uiState: StateFlow<DeviceListUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<DeviceListNavigation>()
    val navigation = _navigation.receiveAsFlow()

    private var isBluetoothEnabled = true

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(dispatcherDefault) {
            observePairedDevicesUseCase.observeAll().collect { devices ->
                _uiState.value = if (devices.isEmpty()) {
                    DeviceListUiState.Empty()
                } else {
                    DeviceListUiState.Content(
                        devices = devices.map { device ->
                            DeviceListItemUiModel(
                                id = device.id,
                                name = device.name,
                                macAddress = device.macAddress,
                                deviceType = device.deviceType,
                                isSessionUsageEnabled = device.isSessionUsageEnabled,
                            )
                        },
                    )
                }
            }
        }
        viewModelScope.launch(dispatcherDefault) {
            bluetoothStateMonitor.observeBluetoothEnabled().collect { isEnabled ->
                isBluetoothEnabled = isEnabled
            }
        }
    }

    fun onEvent(event: DeviceListUiEvent) {
        viewModelScope.launch(dispatcherDefault) {
            when (event) {
                is DeviceListUiEvent.ToggleSessionUsage -> handleToggleSessionUsage(event.id, event.isEnabled)
                is DeviceListUiEvent.DeleteDeviceRequested -> handleDeleteRequested(event.id, event.name)
                is DeviceListUiEvent.DeleteDeviceConfirmed -> handleDeleteConfirmed(event.id)
                is DeviceListUiEvent.DeleteDeviceDismissed -> updateOverlay(null)
                is DeviceListUiEvent.AddDeviceClicked -> handleAddDeviceClicked()
                is DeviceListUiEvent.TestModeClicked -> handleTestModeClicked(event.macAddress)
                is DeviceListUiEvent.ErrorDismissed -> updateOverlay(null)
                is DeviceListUiEvent.BluetoothDisabledDismissed -> updateOverlay(null)
            }
        }
    }

    private suspend fun handleToggleSessionUsage(id: String, isEnabled: Boolean) {
        updateDeviceSessionUsageUseCase.update(id, isEnabled).onFailure { error ->
            updateOverlay(DeviceListOverlay.Error(errorMessageMapper.map(error)))
        }
    }

    private fun handleDeleteRequested(id: String, name: String) {
        updateOverlay(DeviceListOverlay.DeleteConfirmation(deviceId = id, deviceName = name))
    }

    private suspend fun handleDeleteConfirmed(id: String) {
        updateOverlay(null)
        deletePairedDeviceUseCase.delete(id).onFailure { error ->
            updateOverlay(DeviceListOverlay.Error(errorMessageMapper.map(error)))
        }
    }

    private suspend fun handleTestModeClicked(macAddress: String) {
        if (isBluetoothEnabled) {
            _navigation.send(DeviceListNavigation.ToTestMode(macAddress))
        } else {
            updateOverlay(DeviceListOverlay.BluetoothDisabled)
        }
    }

    private fun handleAddDeviceClicked() {
        if (isBluetoothEnabled) {
            viewModelScope.launch(dispatcherDefault) {
                _navigation.send(DeviceListNavigation.ToScanning)
            }
        } else {
            updateOverlay(DeviceListOverlay.BluetoothDisabled)
        }
    }

    private fun updateOverlay(overlay: DeviceListOverlay?) {
        when (val current = _uiState.value) {
            is DeviceListUiState.Content -> _uiState.value = current.copy(overlay = overlay)
            is DeviceListUiState.Empty -> _uiState.value = current.copy(overlay = overlay)
            is DeviceListUiState.Loading -> Unit
        }
    }
}
