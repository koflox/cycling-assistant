package com.koflox.strava.impl.data.mapper

import com.koflox.strava.api.model.SyncErrorReason
import com.koflox.strava.impl.domain.model.StravaError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

class StravaErrorMapperImplTest {

    private val mapper: StravaErrorMapper = StravaErrorMapperImpl()

    @Test
    fun `maps IOException to Network`() {
        assertTrue(mapper.map(IOException("offline")) is StravaError.Network)
    }

    @Test
    fun `maps unknown throwable to Unknown`() {
        assertTrue(mapper.map(IllegalStateException("oops")) is StravaError.Unknown)
    }

    @Test
    fun `passes StravaError through`() {
        val rateLimited = StravaError.RateLimited(retryAfter = 60.seconds)
        assertEquals(rateLimited, mapper.map(rateLimited))
    }

    @Test
    fun `maps 401 to AuthRequired`() = runTest {
        val exception = clientRequestException(HttpStatusCode.Unauthorized)
        assertTrue(mapper.map(exception) is StravaError.AuthRequired)
    }

    @Test
    fun `maps 403 to AuthRequired`() = runTest {
        val exception = clientRequestException(HttpStatusCode.Forbidden)
        assertTrue(mapper.map(exception) is StravaError.AuthRequired)
    }

    @Test
    fun `maps 429 with Retry-After to RateLimited`() = runTest {
        val exception = clientRequestException(
            status = HttpStatusCode.TooManyRequests,
            headers = headersOf(HttpHeaders.RetryAfter, "30"),
        )
        val result = mapper.map(exception)

        assertTrue(result is StravaError.RateLimited)
        assertEquals(SyncErrorReason.RATE_LIMITED, result.reason)
        assertEquals(30.seconds, (result as StravaError.RateLimited).retryAfter)
    }

    @Test
    fun `maps 400 to InvalidActivity`() = runTest {
        val exception = clientRequestException(HttpStatusCode.BadRequest)
        assertTrue(mapper.map(exception) is StravaError.InvalidActivity)
    }

    @Test
    fun `maps 422 to InvalidActivity`() = runTest {
        val exception = clientRequestException(HttpStatusCode.UnprocessableEntity)
        assertTrue(mapper.map(exception) is StravaError.InvalidActivity)
    }

    @Test
    fun `maps 500 to Server`() = runTest {
        val exception = serverResponseException()
        assertTrue(mapper.map(exception) is StravaError.Server)
    }

    private suspend fun clientRequestException(
        status: HttpStatusCode,
        headers: io.ktor.http.Headers = io.ktor.http.Headers.Empty,
    ): ClientRequestException {
        val engine = MockEngine { respond(content = "{}", status = status, headers = headers) }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json() }
            expectSuccess = true
        }
        return runCatching { client.get("http://test/") }
            .exceptionOrNull() as ClientRequestException
    }

    private suspend fun serverResponseException(): ServerResponseException {
        val engine = MockEngine { respond(content = "{}", status = HttpStatusCode.InternalServerError) }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json() }
            expectSuccess = true
        }
        return runCatching { client.get("http://test/") }
            .exceptionOrNull() as ServerResponseException
    }
}
