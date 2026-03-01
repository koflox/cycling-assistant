package com.koflox.connectionsession.bridge.impl.usecase

import com.koflox.connections.domain.model.DeviceType
import com.koflox.connections.domain.model.PairedDevice
import com.koflox.connections.domain.usecase.ObservePairedDevicesUseCase
import com.koflox.connectionsession.bridge.model.PowerReadingData
import com.koflox.connectionsession.bridge.model.SessionPowerDevice
import com.koflox.connectionsession.bridge.usecase.PowerConnectionException
import com.koflox.connectionsession.bridge.usecase.SessionPowerMeterUseCase
import com.koflox.sensor.power.domain.usecase.ObservePowerDataUseCase
import com.koflox.sensor.power.domain.usecase.PowerMeterConnectionException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class SessionPowerMeterUseCaseImpl(
    private val observePairedDevicesUseCase: ObservePairedDevicesUseCase,
    private val observePowerDataUseCase: ObservePowerDataUseCase,
) : SessionPowerMeterUseCase {

    override suspend fun getSessionPowerDevice(): SessionPowerDevice? {
        val devices = observePairedDevicesUseCase.observeAll().first()
        return devices
            .filter { it.isSessionUsageEnabled && it.deviceType == DeviceType.POWER_METER }
            .minByOrNull(PairedDevice::name)
            ?.let { SessionPowerDevice(macAddress = it.macAddress, name = it.name) }
    }

    override fun observePowerReadings(macAddress: String): Flow<PowerReadingData> =
        observePowerDataUseCase.observePowerData(macAddress)
            .map { reading ->
                PowerReadingData(
                    timestampMs = reading.timestampMs,
                    powerWatts = reading.powerWatts,
                    cadenceRpm = reading.cadenceRpm,
                )
            }
            .catch { cause ->
                throw when (cause) {
                    is PowerMeterConnectionException -> PowerConnectionException(cause)
                    else -> PowerConnectionException(cause)
                }
            }

    override fun disconnect() {
        observePowerDataUseCase.disconnect()
    }
}
