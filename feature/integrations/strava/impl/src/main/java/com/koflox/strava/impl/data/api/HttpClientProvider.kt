package com.koflox.strava.impl.data.api

import com.koflox.strava.impl.data.api.HttpClientProvider.createAuthenticated
import com.koflox.strava.impl.data.api.HttpClientProvider.createUnauthenticated
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Builds the two Ktor [HttpClient] instances used to talk to Strava's REST API.
 *
 * Two clients are needed because of a chicken-and-egg problem in OAuth: refreshing the access
 * token is itself an HTTP call, and that call must NOT be subject to the same Bearer-auth
 * machinery that triggered the refresh — otherwise a 401 on the refresh endpoint would
 * recursively try to refresh, looping forever. So:
 *
 * - [createUnauthenticated] — used by the auth endpoints (token exchange, token refresh).
 *   No `Auth` plugin installed; the requests sign themselves with form-encoded
 *   `client_id`/`client_secret`/`code` per Strava's OAuth spec.
 * - [createAuthenticated] — used by everything else (uploads, activities, status polling).
 *   Has Ktor's `Auth` plugin with bearer support wired up to [StravaTokenProvider].
 *
 * ## Auth plugin behavior (the authenticated client)
 *
 * - `loadTokens` is called on every request to attach the current access token. It hits Room
 *   under the hood, so it stays cheap.
 * - `refreshTokens` is called automatically by Ktor when a request comes back 401 — Ktor
 *   re-fires the failed request with the new tokens. If `refreshTokens` returns null
 *   ([StravaTokenProvider] returns null on a refresh failure and wipes the local token), the
 *   original 401 propagates to the caller and gets mapped to `StravaError.AuthRequired`.
 * ## `sendWithoutRequest` — proactive vs. challenge-driven Bearer attachment
 *
 * Ktor's default Auth flow is **challenge-driven** (RFC 7235): the first request goes out
 * unsigned; if the server responds with `401 Unauthorized` + `WWW-Authenticate: Bearer`, the
 * plugin pulls tokens via `loadTokens`, retries the request with the `Authorization: Bearer …`
 * header, and only then bubbles the response back to the caller. That doubles the roundtrip
 * count on every request whose tokens haven't been cached by the plugin yet (and the plugin's
 * cache only lives for the lifetime of a single `HttpClient`).
 *
 * `sendWithoutRequest` flips that to **proactive**: when the predicate returns `true`, Ktor
 * calls `loadTokens` *before* dispatching the request and attaches the Bearer header up front,
 * skipping the 401-then-retry dance entirely. One roundtrip instead of two. We pay for it
 * with a `loadTokens` call (which is just a Room lookup — cheap, see [StravaTokenProvider]).
 *
 * The predicate is host-scoped: `request.url.host == STRAVA_HOST`. Two reasons:
 *
 * 1. **Don't leak tokens.** If Strava's API ever 3xx-redirects to a CDN, S3-signed URL, or
 *    any other domain (Ktor follows redirects by default), the request that hits the
 *    redirected URL is a *new* request that goes through the same Auth plugin. Without the
 *    predicate, our Bearer token would be attached to that follow-up request too — handing
 *    a Strava credential to an unrelated host. The predicate scopes proactive auth strictly
 *    to `www.strava.com`.
 * 2. **Don't waste DB lookups.** `loadTokens` runs a Room query; doing it for non-Strava
 *    hosts (e.g., during redirects or if a unit-test misuses the same client) is pointless.
 *
 * Note that `sendWithoutRequest` only controls the *initial* attachment. After a 401 the
 * `refreshTokens` callback runs and Ktor retries with the new tokens regardless of the
 * predicate — so a token expiring mid-session still recovers transparently.
 *
 * ## Common config (both clients)
 *
 * - `ContentNegotiation` + `kotlinx.serialization` JSON, with `ignoreUnknownKeys` (Strava
 *   adds fields without warning) and `explicitNulls = false` (smaller payloads, our DTOs
 *   tolerate missing keys).
 * - `Logging` at `HEADERS` level only in debug builds — release leaves it `NONE` so we never
 *   print Bearer tokens to logcat.
 * - `expectSuccess = true` — non-2xx responses throw `ClientRequestException` /
 *   `ServerResponseException`. `StravaErrorMapper` catches those and maps to
 *   [com.koflox.strava.impl.domain.model.StravaError]; without this flag we'd have to inspect
 *   `response.status` manually at every call site.
 */
internal object HttpClientProvider {

    private const val STRAVA_HOST = "www.strava.com"

    fun createUnauthenticated(isDebug: Boolean): HttpClient = HttpClient(OkHttp) {
        commonConfig(isDebug)
    }

    fun createAuthenticated(
        isDebug: Boolean,
        loadTokens: suspend () -> BearerTokens?,
        refreshTokens: suspend (refreshToken: String) -> BearerTokens?,
    ): HttpClient = HttpClient(OkHttp) {
        commonConfig(isDebug)
        install(Auth) {
            bearer {
                loadTokens { loadTokens() }
                refreshTokens {
                    val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null
                    refreshTokens(refreshToken)
                }
                sendWithoutRequest { request -> request.url.host == STRAVA_HOST }
            }
        }
    }

    private fun HttpClientConfig<*>.commonConfig(isDebug: Boolean) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }
        install(Logging) {
            level = if (isDebug) LogLevel.HEADERS else LogLevel.NONE
        }
        expectSuccess = true
    }
}
