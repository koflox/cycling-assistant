package com.koflox.sensor.power.domain.usecase

import app.cash.turbine.test
import com.koflox.ble.connection.BleGattManager
import com.koflox.ble.model.BleGattEvent
import com.koflox.sensorprotocol.power.CadenceCalculator
import com.koflox.sensorprotocol.power.CyclingPowerConstants
import com.koflox.sensorprotocol.power.CyclingPowerMeasurement
import com.koflox.sensorprotocol.power.CyclingPowerParser
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ObservePowerDataUseCaseImplTest {

    companion object {
        private const val MAC_ADDRESS = "AA:BB:CC:DD:EE:FF"
        private const val POWER_WATTS = 200
        private const val CRANK_REVOLUTIONS = 42
        private const val LAST_CRANK_EVENT_TIME = 2048
        private const val CADENCE_RPM = 60f
    }

    private val gattManager: BleGattManager = mockk()
    private val parser: CyclingPowerParser = mockk()
    private val cadenceCalculator: CadenceCalculator = mockk()
    private lateinit var useCase: ObservePowerDataUseCaseImpl

    @Before
    fun setup() {
        justRun { gattManager.enableNotifications(any(), any()) }
        useCase = ObservePowerDataUseCaseImpl(
            dispatcherIo = Dispatchers.Unconfined,
            gattManager = gattManager,
            parser = parser,
            cadenceCalculator = cadenceCalculator,
        )
    }

    @Test
    fun `observePowerData parses characteristic changes`() = runTest {
        val data = ByteArray(4)
        val events = flowOf(
            BleGattEvent.ServicesDiscovered,
            BleGattEvent.CharacteristicChanged(
                serviceUuid = CyclingPowerConstants.SERVICE_UUID,
                characteristicUuid = CyclingPowerConstants.MEASUREMENT_CHARACTERISTIC_UUID,
                data = data,
            ),
        )
        every { gattManager.connect(MAC_ADDRESS) } returns events
        every { parser.parse(data) } returns CyclingPowerMeasurement(
            instantaneousPowerWatts = POWER_WATTS,
            crankRevolutions = null,
            lastCrankEventTime = null,
        )

        useCase.observePowerData(MAC_ADDRESS).test {
            val reading = awaitItem()
            assertEquals(POWER_WATTS, reading.powerWatts)
            assertNull(reading.cadenceRpm)
            awaitComplete()
        }
    }

    @Test
    fun `observePowerData calculates cadence when crank data present`() = runTest {
        val data = ByteArray(8)
        val events = flowOf(
            BleGattEvent.ServicesDiscovered,
            BleGattEvent.CharacteristicChanged(
                serviceUuid = CyclingPowerConstants.SERVICE_UUID,
                characteristicUuid = CyclingPowerConstants.MEASUREMENT_CHARACTERISTIC_UUID,
                data = data,
            ),
        )
        every { gattManager.connect(MAC_ADDRESS) } returns events
        every { parser.parse(data) } returns CyclingPowerMeasurement(
            instantaneousPowerWatts = POWER_WATTS,
            crankRevolutions = CRANK_REVOLUTIONS,
            lastCrankEventTime = LAST_CRANK_EVENT_TIME,
        )
        every {
            cadenceCalculator.calculate(CRANK_REVOLUTIONS, LAST_CRANK_EVENT_TIME)
        } returns CADENCE_RPM

        useCase.observePowerData(MAC_ADDRESS).test {
            val reading = awaitItem()
            assertEquals(POWER_WATTS, reading.powerWatts)
            assertEquals(CADENCE_RPM, reading.cadenceRpm)
            awaitComplete()
        }
    }

    @Test
    fun `observePowerData enables notifications on services discovered`() = runTest {
        val events = flowOf(BleGattEvent.ServicesDiscovered)
        every { gattManager.connect(MAC_ADDRESS) } returns events

        useCase.observePowerData(MAC_ADDRESS).test {
            awaitComplete()
        }

        verify {
            gattManager.enableNotifications(
                CyclingPowerConstants.SERVICE_UUID,
                CyclingPowerConstants.MEASUREMENT_CHARACTERISTIC_UUID,
            )
        }
    }

    @Test
    fun `disconnect delegates to gatt manager`() {
        justRun { gattManager.disconnect() }
        useCase.disconnect()
        verify { gattManager.disconnect() }
    }
}
