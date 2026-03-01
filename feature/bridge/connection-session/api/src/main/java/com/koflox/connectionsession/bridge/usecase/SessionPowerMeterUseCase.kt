package com.koflox.connectionsession.bridge.usecase

import com.koflox.connectionsession.bridge.model.PowerReadingData
import com.koflox.connectionsession.bridge.model.SessionPowerDevice
import kotlinx.coroutines.flow.Flow

interface SessionPowerMeterUseCase {
    suspend fun getSessionPowerDevice(): SessionPowerDevice?
    fun observePowerReadings(macAddress: String): Flow<PowerReadingData>
    fun disconnect()
}

class PowerConnectionException(cause: Throwable) : Exception("Failed to connect to power meter", cause)
