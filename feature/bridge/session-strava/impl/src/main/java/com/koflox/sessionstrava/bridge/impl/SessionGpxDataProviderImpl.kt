package com.koflox.sessionstrava.bridge.impl

import com.koflox.gpx.GpxInput
import com.koflox.session.domain.usecase.GetSessionByIdUseCase
import com.koflox.session.presentation.share.toGpxInput
import com.koflox.sessionstrava.bridge.SessionGpxDataProvider
import javax.inject.Inject

internal class SessionGpxDataProviderImpl @Inject constructor(
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
) : SessionGpxDataProvider {

    override suspend fun getGpxInput(sessionId: String): Result<GpxInput> =
        getSessionByIdUseCase.getSession(sessionId).map { it.toGpxInput() }
}
