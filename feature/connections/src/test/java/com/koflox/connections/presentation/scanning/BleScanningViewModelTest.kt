package com.koflox.connections.presentation.scanning

import app.cash.turbine.test
import com.koflox.ble.model.BleDevice
import com.koflox.ble.permission.BlePermissionChecker
import com.koflox.ble.scanner.BleScanner
import com.koflox.connections.domain.model.DeviceType
import com.koflox.connections.domain.model.PairedDevice
import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCase
import com.koflox.connections.domain.usecase.SavePairedDeviceUseCase
import com.koflox.designsystem.text.UiText
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BleScanningViewModelTest {

    companion object {
        private const val MAC_ADDRESS = "AA:BB:CC:DD:EE:FF"
        private const val DEVICE_NAME = "4iiii PRECISION"
        private val ERROR_UI_TEXT = UiText.Resource(com.koflox.error.R.string.error_not_handled)
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bleScanner: BleScanner = mockk()
    private val blePermissionChecker: BlePermissionChecker = mockk()
    private val observePairedDevicesUseCase: ObservePairedDevicesUseCase = mockk()
    private val savePairedDeviceUseCase: SavePairedDeviceUseCase = mockk()
    private val errorMessageMapper: ErrorMessageMapper = mockk()
    private lateinit var viewModel: BleScanningViewModel

    @Before
    fun setup() {
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        every { blePermissionChecker.hasPermissions() } returns true
        every { observePairedDevicesUseCase.observeAll() } returns flowOf(emptyList())
        coEvery { errorMessageMapper.map(any()) } returns ERROR_UI_TEXT
    }

    private fun createViewModel() = BleScanningViewModel(
        bleScanner = bleScanner,
        blePermissionChecker = blePermissionChecker,
        observePairedDevicesUseCase = observePairedDevicesUseCase,
        savePairedDeviceUseCase = savePairedDeviceUseCase,
        errorMessageMapper = errorMessageMapper,
        dispatcherDefault = mainDispatcherRule.testDispatcher,
    )

    @Test
    fun `initial state without permissions shows PermissionRequired`() = runTest {
        every { blePermissionChecker.hasPermissions() } returns false
        viewModel = createViewModel()
        viewModel.uiState.test {
            assertTrue(awaitItem() is BleScanningUiState.PermissionRequired)
        }
    }

    @Test
    fun `initial state with permissions starts Scanning`() = runTest {
        every { bleScanner.scan(any(), any()) } returns flowOf()
        viewModel = createViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is BleScanningUiState.Scanning)
        }
    }

    @Test
    fun `scan results show discovered devices`() = runTest {
        val device = BleDevice(
            address = MAC_ADDRESS,
            name = DEVICE_NAME,
            serviceUuids = emptyList(),
        )
        every { bleScanner.scan(any(), any()) } returns flowOf(device)
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Empty scanning
            val scanning = awaitItem() as BleScanningUiState.Scanning
            assertEquals(1, scanning.devices.size)
            assertEquals(MAC_ADDRESS, scanning.devices[0].macAddress)
            assertEquals(DEVICE_NAME, scanning.devices[0].name)
        }
    }

    @Test
    fun `device marked as already paired when in paired list`() = runTest {
        val device = BleDevice(
            address = MAC_ADDRESS,
            name = DEVICE_NAME,
            serviceUuids = emptyList(),
        )
        val pairedDevice = PairedDevice(
            id = "id-1",
            macAddress = MAC_ADDRESS,
            name = DEVICE_NAME,
            deviceType = DeviceType.POWER_METER,
            isSessionUsageEnabled = false,
        )
        every { observePairedDevicesUseCase.observeAll() } returns flowOf(listOf(pairedDevice))
        every { bleScanner.scan(any(), any()) } returns flowOf(device)
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Empty scanning
            val scanning = awaitItem() as BleScanningUiState.Scanning
            assertTrue(scanning.devices[0].isAlreadyPaired)
        }
    }

    @Test
    fun `DeviceSelected saves device and dismisses`() = runTest {
        every { bleScanner.scan(any(), any()) } returns flowOf()
        coEvery {
            savePairedDeviceUseCase.save(MAC_ADDRESS, DEVICE_NAME, DeviceType.POWER_METER)
        } returns Result.success(Unit)
        viewModel = createViewModel()
        viewModel.isDismissed.test {
            assertEquals(false, awaitItem())
            viewModel.onEvent(BleScanningUiEvent.DeviceSelected(MAC_ADDRESS, DEVICE_NAME))
            assertEquals(true, awaitItem())
        }
    }
}
