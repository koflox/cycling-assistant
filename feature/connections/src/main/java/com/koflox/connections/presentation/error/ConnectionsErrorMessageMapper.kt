package com.koflox.connections.presentation.error

import com.koflox.connections.R
import com.koflox.connections.domain.usecase.DeviceAlreadyPairedException
import com.koflox.designsystem.text.UiText
import com.koflox.error.mapper.ErrorMessageMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class ConnectionsErrorMessageMapper(
    private val defaultMapper: ErrorMessageMapper,
    private val dispatcherDefault: CoroutineDispatcher,
) : ErrorMessageMapper {

    override suspend fun map(error: Throwable): UiText = withContext(dispatcherDefault) {
        when (error) {
            is DeviceAlreadyPairedException -> UiText.Resource(R.string.connections_error_save_failed)
            else -> defaultMapper.map(error)
        }
    }
}
