package com.koflox.sessionsettings.bridge.impl.usecase

import com.koflox.sessionsettings.bridge.api.RiderProfileUseCase
import com.koflox.settings.api.RiderProfileProvider

internal class RiderProfileUseCaseImpl(
    private val riderProfileProvider: RiderProfileProvider,
) : RiderProfileUseCase {

    override suspend fun getRiderWeightKg(): Float? = riderProfileProvider.getRiderWeightKg()
}
