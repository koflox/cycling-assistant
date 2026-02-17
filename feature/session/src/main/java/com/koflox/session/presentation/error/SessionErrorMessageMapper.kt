package com.koflox.session.presentation.error

import com.koflox.designsystem.text.UiText
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.location.error.LocationUnavailableException
import com.koflox.session.R
import com.koflox.session.domain.usecase.NoActiveSessionException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class SessionErrorMessageMapper(
    private val dispatcherDefault: CoroutineDispatcher,
    private val defaultMapper: ErrorMessageMapper,
) : ErrorMessageMapper {

    override suspend fun map(error: Throwable): UiText = withContext(dispatcherDefault) {
        when (error) {
            is LocationUnavailableException -> UiText.Resource(R.string.error_location_unavailable)
            is NoActiveSessionException -> UiText.Resource(R.string.error_no_active_session)
            else -> defaultMapper.map(error)
        }
    }
}
