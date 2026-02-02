package com.koflox.sessionsettings.bridge.api

interface RiderProfileUseCase {
    suspend fun getRiderWeightKg(): Float?
}
