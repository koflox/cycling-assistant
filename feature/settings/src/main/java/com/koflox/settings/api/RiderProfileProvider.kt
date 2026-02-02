package com.koflox.settings.api

interface RiderProfileProvider {
    suspend fun getRiderWeightKg(): Float?
}
