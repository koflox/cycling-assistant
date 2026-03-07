package com.koflox.sensor.power.domain.usecase

import com.koflox.ble.connection.BleGattManager
import com.koflox.ble.model.BleGattEvent
import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.sensor.power.domain.model.PowerReading
import com.koflox.sensorprotocol.power.CadenceCalculator
import com.koflox.sensorprotocol.power.CyclingPowerConstants
import com.koflox.sensorprotocol.power.CyclingPowerParser
import com.koflox.sensorprotocol.power.WheelSpeedCalculator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

interface ObservePowerDataUseCase {
    fun observePowerData(macAddress: String): Flow<PowerReading>
    fun disconnect()
}

class PowerMeterConnectionException(cause: Throwable) :
    Exception("Failed to connect to power meter", cause)

internal class ObservePowerDataUseCaseImpl(
    private val dispatcherIo: CoroutineDispatcher,
    private val gattManager: BleGattManager,
    private val parser: CyclingPowerParser,
    private val cadenceCalculator: CadenceCalculator,
    private val wheelSpeedCalculator: WheelSpeedCalculator,
    private val currentTimeProvider: CurrentTimeProvider,
) : ObservePowerDataUseCase {

    override fun observePowerData(macAddress: String): Flow<PowerReading> =
        gattManager.connect(macAddress)
            .onEach { event ->
                if (event is BleGattEvent.ServicesDiscovered) {
                    gattManager.enableNotifications(
                        CyclingPowerConstants.SERVICE_UUID,
                        CyclingPowerConstants.MEASUREMENT_CHARACTERISTIC_UUID,
                    )
                }
            }
            .filter { it is BleGattEvent.CharacteristicChanged }
            .mapNotNull { event ->
                val changed = event as BleGattEvent.CharacteristicChanged
                if (changed.characteristicUuid != CyclingPowerConstants.MEASUREMENT_CHARACTERISTIC_UUID) {
                    return@mapNotNull null
                }
                val measurement = parser.parse(changed.data) ?: return@mapNotNull null
                val crankRevs = measurement.crankRevolutions
                val crankTime = measurement.lastCrankEventTime
                val cadence = if (crankRevs != null && crankTime != null) {
                    cadenceCalculator.calculate(crankRevs, crankTime)
                } else {
                    null
                }
                val wheelRevs = measurement.cumulativeWheelRevolutions
                val wheelTime = measurement.lastWheelEventTime
                val wheelSpeed = if (wheelRevs != null && wheelTime != null) {
                    wheelSpeedCalculator.calculate(wheelRevs, wheelTime)
                } else {
                    null
                }
                PowerReading(
                    timestampMs = currentTimeProvider.currentTimeMs(),
                    powerWatts = measurement.instantaneousPowerWatts,
                    cadenceRpm = cadence,
                    pedalPowerBalancePercent = measurement.pedalPowerBalancePercent,
                    accumulatedTorqueNm = measurement.accumulatedTorqueNm,
                    wheelSpeedKmh = wheelSpeed,
                    accumulatedEnergyKj = measurement.accumulatedEnergyKj,
                )
            }
            .catch { throw PowerMeterConnectionException(it) }
            .flowOn(dispatcherIo)

    override fun disconnect() {
        gattManager.disconnect()
    }
}
