package com.koflox.strava.impl.data.mapper

import com.koflox.strava.impl.domain.model.StravaError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

internal interface StravaErrorMapper {
    fun map(throwable: Throwable): StravaError
}

internal class StravaErrorMapperImpl @Inject constructor() : StravaErrorMapper {

    override fun map(throwable: Throwable): StravaError = when (throwable) {
        is StravaError -> throwable
        is ClientRequestException -> mapClientError(throwable)
        is ServerResponseException -> StravaError.Server()
        is ResponseException -> StravaError.Unknown()
        is ConnectTimeoutException, is SocketTimeoutException, is IOException -> StravaError.Network()
        else -> StravaError.Unknown()
    }

    private fun mapClientError(exception: ClientRequestException): StravaError {
        val response = exception.response
        return when (response.status) {
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> StravaError.AuthRequired()
            HttpStatusCode.TooManyRequests -> StravaError.RateLimited(parseRetryAfter(response.headers[HttpHeaders.RetryAfter]))
            HttpStatusCode.BadRequest, HttpStatusCode.UnprocessableEntity -> StravaError.InvalidActivity()
            else -> if (response.status.value in 500..599) StravaError.Server() else StravaError.Unknown()
        }
    }

    private fun parseRetryAfter(header: String?): kotlin.time.Duration? =
        header?.toLongOrNull()?.seconds
}
