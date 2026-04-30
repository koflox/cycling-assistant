package com.koflox.strava.impl.oauth

import com.koflox.di.DefaultDispatcher
import com.koflox.strava.impl.domain.usecase.StravaAuthUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-scoped handler for the Strava OAuth authorization code. Exchanges the code for
 * an access token on its own [SupervisorJob]-backed scope so the work survives the lifecycle
 * of [StravaOAuthRedirectActivity], which must `finish()` synchronously to satisfy the
 * `Theme.NoDisplay` contract.
 *
 * Also validates the scopes Strava actually granted (the consent UI lets the user untick any
 * scope checkbox) — if [StravaAuthUrlBuilder.REQUIRED_SCOPES] are not all present, we drop
 * the code, force a logout, and surface a hint via [StravaAuthEvents] so the Connect screen
 * can prompt the user to re-authorize.
 */
@Singleton
internal class StravaOAuthCodeProcessor @Inject constructor(
    private val authUseCase: StravaAuthUseCase,
    private val authEvents: StravaAuthEvents,
    @DefaultDispatcher dispatcherDefault: CoroutineDispatcher,
) {

    private companion object {
        const val SCOPE_SEPARATOR = ","
    }

    private val scope = CoroutineScope(SupervisorJob() + dispatcherDefault)

    fun process(authorizationCode: String, grantedScope: String) {
        val granted = grantedScope
            .split(SCOPE_SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
        if (!granted.containsAll(StravaAuthUrlBuilder.REQUIRED_SCOPES)) {
            scope.launch {
                authUseCase.logout()
                authEvents.report(StravaAuthHint.MissingRequiredScopes)
            }
            return
        }
        scope.launch {
            authUseCase.login(authorizationCode)
        }
    }
}
