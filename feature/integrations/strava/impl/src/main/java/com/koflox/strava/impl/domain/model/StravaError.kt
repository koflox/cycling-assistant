package com.koflox.strava.impl.domain.model

import com.koflox.strava.api.model.SyncErrorReason
import kotlin.time.Duration

internal sealed class StravaError(
    val reason: SyncErrorReason,
    val isRetryable: Boolean,
) : Throwable() {

    class Network : StravaError(SyncErrorReason.NETWORK, isRetryable = true)

    class AuthRequired : StravaError(SyncErrorReason.AUTH_REQUIRED, isRetryable = false)

    class RateLimited(val retryAfter: Duration?) :
        StravaError(SyncErrorReason.RATE_LIMITED, isRetryable = true)

    class InvalidActivity : StravaError(SyncErrorReason.INVALID_ACTIVITY, isRetryable = false)

    class Server : StravaError(SyncErrorReason.SERVER, isRetryable = true)

    class Unknown : StravaError(SyncErrorReason.UNKNOWN, isRetryable = false)
}
