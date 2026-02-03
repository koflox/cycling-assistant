package com.koflox.sessionsettings.bridge.impl.usecase

import com.koflox.profile.domain.usecase.GetRiderWeightUseCase
import com.koflox.sessionsettings.bridge.api.RiderProfileUseCase

internal class RiderProfileUseCaseImpl(
    private val getRiderWeightUseCase: GetRiderWeightUseCase,
) : RiderProfileUseCase {

    override suspend fun getRiderWeightKg(): Float? = getRiderWeightUseCase.getRiderWeightKg()
}
