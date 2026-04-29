package com.koflox.strava.impl.data.api

import com.koflox.strava.impl.data.api.dto.TokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import javax.inject.Inject

internal interface StravaAuthApi {
    suspend fun exchangeAuthorizationCode(code: String): TokenResponse
    suspend fun refreshToken(refreshToken: String): TokenResponse
}

internal class StravaAuthApiImpl @Inject constructor(
    private val client: HttpClient,
    private val clientCredentials: StravaClientCredentials,
) : StravaAuthApi {

    private companion object {
        const val TOKEN_URL = "https://www.strava.com/oauth/token"
        const val PARAM_CLIENT_ID = "client_id"
        const val PARAM_CLIENT_SECRET = "client_secret"
        const val PARAM_CODE = "code"
        const val PARAM_GRANT_TYPE = "grant_type"
        const val REFRESH_TOKEN_LITERAL = "refresh_token"
        const val PARAM_REFRESH_TOKEN = REFRESH_TOKEN_LITERAL
        const val GRANT_AUTH_CODE = "authorization_code"
        const val GRANT_REFRESH_TOKEN = REFRESH_TOKEN_LITERAL
    }

    override suspend fun exchangeAuthorizationCode(code: String): TokenResponse =
        client.submitForm(
            url = TOKEN_URL,
            formParameters = Parameters.build {
                append(PARAM_CLIENT_ID, clientCredentials.clientId)
                append(PARAM_CLIENT_SECRET, clientCredentials.clientSecret)
                append(PARAM_CODE, code)
                append(PARAM_GRANT_TYPE, GRANT_AUTH_CODE)
            },
        ).body()

    override suspend fun refreshToken(refreshToken: String): TokenResponse =
        client.submitForm(
            url = TOKEN_URL,
            formParameters = Parameters.build {
                append(PARAM_CLIENT_ID, clientCredentials.clientId)
                append(PARAM_CLIENT_SECRET, clientCredentials.clientSecret)
                append(PARAM_REFRESH_TOKEN, refreshToken)
                append(PARAM_GRANT_TYPE, GRANT_REFRESH_TOKEN)
            },
        ).body()

}
