package com.koflox.strava.impl.oauth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

internal enum class StravaAuthHint {
    MissingRequiredScopes,
}

/**
 * Singleton bus for transient auth hints surfaced from the OAuth callback flow back to the
 * Connect screen. The redirect activity finishes immediately, so the Connect ViewModel may
 * not be in scope at the moment a hint is produced — using a [StateFlow] allows the hint to
 * be observed once the user returns to the screen.
 */
@Singleton
internal class StravaAuthEvents @Inject constructor() {

    private val _hint = MutableStateFlow<StravaAuthHint?>(null)
    val hint: StateFlow<StravaAuthHint?> = _hint.asStateFlow()

    fun report(hint: StravaAuthHint) {
        _hint.value = hint
    }

    fun consume() {
        _hint.value = null
    }
}
