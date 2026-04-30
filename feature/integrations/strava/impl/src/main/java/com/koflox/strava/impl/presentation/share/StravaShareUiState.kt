package com.koflox.strava.impl.presentation.share

import com.koflox.strava.api.model.SessionSyncStatus
import com.koflox.strava.api.model.StravaAuthState

internal sealed interface StravaShareUiState {

    data object Loading : StravaShareUiState

    data class Content(
        val authState: StravaAuthState,
        val syncStatus: SessionSyncStatus,
        val refreshCooldownSeconds: Int = 0,
    ) : StravaShareUiState
}
