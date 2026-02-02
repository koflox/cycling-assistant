package com.koflox.session.presentation.error

import android.content.Context
import com.koflox.error.mapper.ErrorMessageMapper
import com.koflox.session.R
import com.koflox.location.LocationUnavailableException
import com.koflox.session.domain.usecase.NoActiveSessionException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class SessionErrorMessageMapper(
    private val context: Context,
    private val dispatcherDefault: CoroutineDispatcher,
    private val defaultMapper: ErrorMessageMapper,
) : ErrorMessageMapper {

    override suspend fun map(error: Throwable): String = withContext(dispatcherDefault) {
        when (error) {
            is LocationUnavailableException -> context.getString(R.string.error_location_unavailable)
            is NoActiveSessionException -> context.getString(R.string.error_no_active_session)
            else -> defaultMapper.map(error)
        }
    }
}
