package com.koflox.profilesession.bridge.api

interface RiderProfileUseCase {
    suspend fun getRiderWeightKg(): Float?
}
