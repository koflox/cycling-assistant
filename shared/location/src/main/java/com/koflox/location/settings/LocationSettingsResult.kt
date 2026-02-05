package com.koflox.location.settings

import android.content.IntentSender

sealed interface LocationSettingsResult {
    data object Enabled : LocationSettingsResult
    data class ResolutionRequired(val intentSender: IntentSender) : LocationSettingsResult
    data object Unavailable : LocationSettingsResult
}
