package com.koflox.connectionsession.bridge.impl.usecase

import app.cash.turbine.test
import com.koflox.connections.domain.model.DeviceType
import com.koflox.connections.domain.model.PairedDevice
import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCase
import com.koflox.connectionsession.bridge.usecase.PowerConnectionException
import com.koflox.sensor.power.domain.model.PowerReading
import com.koflox.sensor.power.domain.usecase.ObservePowerDataUseCase
import com.koflox.sensor.power.domain.usecase.PowerMeterConnectionException
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionPowerMeterUseCaseImplTest {

    companion object {
        private const val MAC_ADDRESS = "AA:BB:CC:DD:EE:FF"
        private const val DEVICE_NAME_A = "4iiii Alpha"
        private const val DEVICE_NAME_B = "4iiii Beta"
        private const val DEVICE_ID = "device-1"
        private const val TIMESTAMP_MS = 1000L
        private const val POWER_WATTS = 200
        private const val CADENCE_RPM = 90f
    }

    private val observePairedDevicesUseCase: ObservePairedDevicesUseCase = io.mockk.mockk()
    private val observePowerDataUseCase: ObservePowerDataUseCase = io.mockk.mockk()
    private lateinit var useCase: SessionPowerMeterUseCaseImpl

    @Before
    fun setup() {
        useCase = SessionPowerMeterUseCaseImpl(
            observePairedDevicesUseCase = observePairedDevicesUseCase,
            observePowerDataUseCase = observePowerDataUseCase,
        )
    }

    @Test
    fun `getSessionPowerDevice returns first session-enabled power meter sorted by name`() = runTest {
        every { observePairedDevicesUseCase.observeAll() } returns flowOf(
            listOf(
                createPairedDevice(name = DEVICE_NAME_B, isSessionUsageEnabled = true),
                createPairedDevice(name = DEVICE_NAME_A, isSessionUsageEnabled = true),
            ),
        )
        val result = useCase.getSessionPowerDevice()
        assertEquals(DEVICE_NAME_A, result?.name)
    }

    @Test
    fun `getSessionPowerDevice returns null when no session-enabled devices`() = runTest {
        every { observePairedDevicesUseCase.observeAll() } returns flowOf(
            listOf(createPairedDevice(isSessionUsageEnabled = false)),
        )
        val result = useCase.getSessionPowerDevice()
        assertNull(result)
    }

    @Test
    fun `getSessionPowerDevice returns null when no devices`() = runTest {
        every { observePairedDevicesUseCase.observeAll() } returns flowOf(emptyList())
        val result = useCase.getSessionPowerDevice()
        assertNull(result)
    }

    @Test
    fun `observePowerReadings maps PowerReading to PowerReadingData`() = runTest {
        every { observePowerDataUseCase.observePowerData(MAC_ADDRESS) } returns flowOf(
            PowerReading(timestampMs = TIMESTAMP_MS, powerWatts = POWER_WATTS, cadenceRpm = CADENCE_RPM),
        )
        useCase.observePowerReadings(MAC_ADDRESS).test {
            val reading = awaitItem()
            assertEquals(TIMESTAMP_MS, reading.timestampMs)
            assertEquals(POWER_WATTS, reading.powerWatts)
            assertEquals(CADENCE_RPM, reading.cadenceRpm)
            awaitComplete()
        }
    }

    @Test
    fun `observePowerReadings wraps PowerMeterConnectionException as PowerConnectionException`() = runTest {
        every { observePowerDataUseCase.observePowerData(MAC_ADDRESS) } returns flow {
            throw PowerMeterConnectionException(RuntimeException("BLE error"))
        }
        useCase.observePowerReadings(MAC_ADDRESS).test {
            val error = awaitError()
            assertTrue(error is PowerConnectionException)
        }
    }

    @Test
    fun `disconnect delegates to ObservePowerDataUseCase`() {
        justRun { observePowerDataUseCase.disconnect() }
        useCase.disconnect()
        verify { observePowerDataUseCase.disconnect() }
    }

    private fun createPairedDevice(
        id: String = DEVICE_ID,
        macAddress: String = MAC_ADDRESS,
        name: String = DEVICE_NAME_A,
        deviceType: DeviceType = DeviceType.POWER_METER,
        isSessionUsageEnabled: Boolean = true,
    ) = PairedDevice(
        id = id,
        macAddress = macAddress,
        name = name,
        deviceType = deviceType,
        isSessionUsageEnabled = isSessionUsageEnabled,
    )
}
