package com.koflox.connections.presentation.listing

import app.cash.turbine.test
import com.koflox.ble.state.BluetoothStateMonitor
import com.koflox.connections.domain.model.DeviceType
import com.koflox.connections.domain.model.PairedDevice
import com.koflox.connections.domain.usecase.DeletePairedDeviceUseCase
import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCase
import com.koflox.connections.domain.usecase.UpdateDeviceSessionUsageUseCase
import com.koflox.designsystem.text.UiText
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeviceListViewModelTest {

    companion object {
        private const val DEVICE_ID = "device-123"
        private const val DEVICE_NAME = "4iiii PRECISION"
        private const val MAC_ADDRESS = "AA:BB:CC:DD:EE:FF"
        private val ERROR_UI_TEXT = UiText.Resource(com.koflox.error.R.string.error_not_handled)
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observePairedDevicesUseCase: ObservePairedDevicesUseCase = mockk()
    private val deletePairedDeviceUseCase: DeletePairedDeviceUseCase = mockk()
    private val updateDeviceSessionUsageUseCase: UpdateDeviceSessionUsageUseCase = mockk()
    private val bluetoothStateMonitor: BluetoothStateMonitor = mockk()
    private val errorMessageMapper: ErrorMessageMapper = mockk()
    private lateinit var viewModel: DeviceListViewModel

    private val bluetoothEnabledFlow = MutableStateFlow(true)

    @Before
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        every { bluetoothStateMonitor.observeBluetoothEnabled() } returns bluetoothEnabledFlow
        coEvery { errorMessageMapper.map(any()) } returns ERROR_UI_TEXT
    }

    private fun createViewModel() = DeviceListViewModel(
        observePairedDevicesUseCase = observePairedDevicesUseCase,
        deletePairedDeviceUseCase = deletePairedDeviceUseCase,
        updateDeviceSessionUsageUseCase = updateDeviceSessionUsageUseCase,
        bluetoothStateMonitor = bluetoothStateMonitor,
        errorMessageMapper = errorMessageMapper,
        dispatcherDefault = mainDispatcherRule.testDispatcher,
    )

    @Test
    fun `initial state is Loading`() = runTest {
        every { observePairedDevicesUseCase.observeAll() } returns flowOf(emptyList())
        viewModel = createViewModel()
        viewModel.uiState.test {
            assertTrue(awaitItem() is DeviceListUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty devices shows Empty state`() = runTest {
        every { observePairedDevicesUseCase.observeAll() } returns flowOf(emptyList())
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            assertTrue(awaitItem() is DeviceListUiState.Empty)
        }
    }

    @Test
    fun `devices loaded shows Content state`() = runTest {
        val device = createDevice()
        every { observePairedDevicesUseCase.observeAll() } returns flowOf(listOf(device))
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val content = awaitItem() as DeviceListUiState.Content
            assertEquals(1, content.devices.size)
            assertEquals(DEVICE_NAME, content.devices[0].name)
            assertEquals(MAC_ADDRESS, content.devices[0].macAddress)
        }
    }

    @Test
    fun `DeleteDeviceRequested shows confirmation overlay`() = runTest {
        val device = createDevice()
        every { observePairedDevicesUseCase.observeAll() } returns flowOf(listOf(device))
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(DeviceListUiEvent.DeleteDeviceRequested(DEVICE_ID, DEVICE_NAME))
            val content = awaitItem() as DeviceListUiState.Content
            val overlay = content.overlay as DeviceListOverlay.DeleteConfirmation
            assertEquals(DEVICE_ID, overlay.deviceId)
            assertEquals(DEVICE_NAME, overlay.deviceName)
        }
    }

    @Test
    fun `DeleteDeviceConfirmed clears overlay`() = runTest {
        val devicesFlow = MutableStateFlow(listOf(createDevice()))
        every { observePairedDevicesUseCase.observeAll() } returns devicesFlow
        coEvery { deletePairedDeviceUseCase.delete(DEVICE_ID) } returns Result.success(Unit)
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(DeviceListUiEvent.DeleteDeviceRequested(DEVICE_ID, DEVICE_NAME))
            awaitItem() // Confirmation overlay
            viewModel.onEvent(DeviceListUiEvent.DeleteDeviceConfirmed(DEVICE_ID))
            val content = awaitItem() as DeviceListUiState.Content
            assertNull(content.overlay)
        }
    }

    @Test
    fun `AddDeviceClicked with bluetooth disabled shows overlay`() = runTest {
        every { observePairedDevicesUseCase.observeAll() } returns flowOf(listOf(createDevice()))
        bluetoothEnabledFlow.value = false
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content
            viewModel.onEvent(DeviceListUiEvent.AddDeviceClicked)
            val content = awaitItem() as DeviceListUiState.Content
            assertTrue(content.overlay is DeviceListOverlay.BluetoothDisabled)
        }
    }

    private fun createDevice(
        id: String = DEVICE_ID,
        macAddress: String = MAC_ADDRESS,
        name: String = DEVICE_NAME,
        deviceType: DeviceType = DeviceType.POWER_METER,
        isSessionUsageEnabled: Boolean = false,
    ) = PairedDevice(
        id = id,
        macAddress = macAddress,
        name = name,
        deviceType = deviceType,
        isSessionUsageEnabled = isSessionUsageEnabled,
    )
}
