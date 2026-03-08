package com.koflox.session.service

import kotlin.time.Duration

internal sealed interface PowerConnectionState {
    data object Connecting : PowerConnectionState
    data class Connected(val instantaneousPowerWatts: Int) : PowerConnectionState
    data class Reconnecting(val remaining: Duration) : PowerConnectionState
}
