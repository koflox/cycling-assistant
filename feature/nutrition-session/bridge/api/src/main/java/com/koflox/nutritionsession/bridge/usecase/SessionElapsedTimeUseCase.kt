package com.koflox.nutritionsession.bridge.usecase

import com.koflox.nutritionsession.bridge.model.SessionTimeInfo
import kotlinx.coroutines.flow.Flow

interface SessionElapsedTimeUseCase {
    fun observeSessionTimeInfo(): Flow<SessionTimeInfo?>
}
