package com.koflox.session.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PendingSessionAction {
    val isStopRequested: StateFlow<Boolean>
    fun handleIntentAction(action: String)
}

internal interface PendingSessionActionConsumer {
    fun consumeStopRequest()
}

internal class PendingSessionActionImpl : PendingSessionAction, PendingSessionActionConsumer {

    companion object {
        const val ACTION_STOP_CONFIRMATION = "com.koflox.session.STOP_CONFIRMATION"
    }

    private val _isStopRequested = MutableStateFlow(false)
    override val isStopRequested: StateFlow<Boolean> = _isStopRequested.asStateFlow()

    override fun handleIntentAction(action: String) {
        if (action == ACTION_STOP_CONFIRMATION) {
            _isStopRequested.value = true
        }
    }

    override fun consumeStopRequest() {
        _isStopRequested.value = false
    }
}
