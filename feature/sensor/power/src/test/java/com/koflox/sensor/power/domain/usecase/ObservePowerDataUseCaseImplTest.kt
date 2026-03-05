package com.koflox.sensor.power.domain.usecase

import app.cash.turbine.test
import com.koflox.ble.connection.BleGattManager
import com.koflox.ble.model.BleGattEvent
import com.koflox.sensorprotocol.power.CadenceCalculator
import com.koflox.sensorprotocol.power.CyclingPowerConstants
import com.koflox.sensorprotocol.power.CyclingPowerMeasurement
import com.koflox.sensorprotocol.power.CyclingPowerParser
import com.koflox.sensorprotocol.power.WheelSpeedCalculator
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
        private const val PEDAL_BALANCE = 50.5f
        private const val TORQUE_NM = 12.3f
        private const val WHEEL_REVOLUTIONS = 1000L
        private const val LAST_WHEEL_EVENT_TIME = 4096
        private const val WHEEL_SPEED_KMH = 25.3f
        private const val ACCUMULATED_ENERGY_KJ = 42
    }

    private val gattManager: BleGattManager = mockk()
    private val parser: CyclingPowerParser = mockk()
    private val cadenceCalculator: CadenceCalculator = mockk()
    private val wheelSpeedCalculator: WheelSpeedCalculator = mockk()
    private lateinit var useCase: ObservePowerDataUseCaseImpl

    @Before
    fun setup() {
        justRun { gattManager.enableNotifications(any(), any()) }
        useCase = ObservePowerDataUseCaseImpl(
            dispatcherIo = Dispatchers.Unconfined,
            gattManager = gattManager,
            parser = parser,
            cadenceCalculator = cadenceCalculator,
            wheelSpeedCalculator = wheelSpeedCalculator,
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
        every { parser.parse(data) } returns createMeasurement()

        useCase.observePowerData(MAC_ADDRESS).test {
            val reading = awaitItem()
            assertEquals(POWER_WATTS, reading.powerWatts)
            assertNull(reading.cadenceRpm)
            assertNull(reading.wheelSpeedKmh)
            assertNull(reading.pedalPowerBalancePercent)
            assertNull(reading.accumulatedTorqueNm)
            assertNull(reading.accumulatedEnergyKj)
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
        every { parser.parse(data) } returns createMeasurement(
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
    fun `observePowerData calculates wheel speed when wheel data present`() = runTest {
        val data = ByteArray(10)
        val events = flowOf(
            BleGattEvent.ServicesDiscovered,
            BleGattEvent.CharacteristicChanged(
                serviceUuid = CyclingPowerConstants.SERVICE_UUID,
                characteristicUuid = CyclingPowerConstants.MEASUREMENT_CHARACTERISTIC_UUID,
                data = data,
            ),
        )
        every { gattManager.connect(MAC_ADDRESS) } returns events
        every { parser.parse(data) } returns createMeasurement(
            cumulativeWheelRevolutions = WHEEL_REVOLUTIONS,
            lastWheelEventTime = LAST_WHEEL_EVENT_TIME,
        )
        every {
            wheelSpeedCalculator.calculate(WHEEL_REVOLUTIONS, LAST_WHEEL_EVENT_TIME)
        } returns WHEEL_SPEED_KMH

        useCase.observePowerData(MAC_ADDRESS).test {
            val reading = awaitItem()
            assertEquals(WHEEL_SPEED_KMH, reading.wheelSpeedKmh)
            awaitComplete()
        }
    }

    @Test
    fun `observePowerData passes through sensor fields`() = runTest {
        val data = ByteArray(16)
        val events = flowOf(
            BleGattEvent.ServicesDiscovered,
            BleGattEvent.CharacteristicChanged(
                serviceUuid = CyclingPowerConstants.SERVICE_UUID,
                characteristicUuid = CyclingPowerConstants.MEASUREMENT_CHARACTERISTIC_UUID,
                data = data,
            ),
        )
        every { gattManager.connect(MAC_ADDRESS) } returns events
        every { parser.parse(data) } returns createMeasurement(
            pedalPowerBalancePercent = PEDAL_BALANCE,
            accumulatedTorqueNm = TORQUE_NM,
            accumulatedEnergyKj = ACCUMULATED_ENERGY_KJ,
        )

        useCase.observePowerData(MAC_ADDRESS).test {
            val reading = awaitItem()
            assertEquals(PEDAL_BALANCE, reading.pedalPowerBalancePercent)
            assertEquals(TORQUE_NM, reading.accumulatedTorqueNm)
            assertEquals(ACCUMULATED_ENERGY_KJ, reading.accumulatedEnergyKj)
            awaitComplete()
        }
    }

    @Test
    fun `observePowerData skips null parser result`() = runTest {
        val data = ByteArray(1)
        val events = flowOf(
            BleGattEvent.ServicesDiscovered,
            BleGattEvent.CharacteristicChanged(
                serviceUuid = CyclingPowerConstants.SERVICE_UUID,
                characteristicUuid = CyclingPowerConstants.MEASUREMENT_CHARACTERISTIC_UUID,
                data = data,
            ),
        )
        every { gattManager.connect(MAC_ADDRESS) } returns events
        every { parser.parse(data) } returns null

        useCase.observePowerData(MAC_ADDRESS).test {
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

    private fun createMeasurement(
        power: Int = POWER_WATTS,
        pedalPowerBalancePercent: Float? = null,
        accumulatedTorqueNm: Float? = null,
        cumulativeWheelRevolutions: Long? = null,
        lastWheelEventTime: Int? = null,
        crankRevolutions: Int? = null,
        lastCrankEventTime: Int? = null,
        accumulatedEnergyKj: Int? = null,
    ) = CyclingPowerMeasurement(
        instantaneousPowerWatts = power,
        pedalPowerBalancePercent = pedalPowerBalancePercent,
        accumulatedTorqueNm = accumulatedTorqueNm,
        cumulativeWheelRevolutions = cumulativeWheelRevolutions,
        lastWheelEventTime = lastWheelEventTime,
        crankRevolutions = crankRevolutions,
        lastCrankEventTime = lastCrankEventTime,
        accumulatedEnergyKj = accumulatedEnergyKj,
    )
}
