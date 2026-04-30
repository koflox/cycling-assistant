package com.koflox.strava.impl.presentation.connect

import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.impl.oauth.StravaAuthHint

internal sealed interface StravaConnectUiState {

    data object Loading : StravaConnectUiState

    data class Content(
        val authState: StravaAuthState,
        val overlay: Overlay? = null,
        val hint: StravaAuthHint? = null,
    ) : StravaConnectUiState {

        sealed interface Overlay {
            data object LogoutConfirm : Overlay
        }
    }
}
