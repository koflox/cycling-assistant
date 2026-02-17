package com.koflox.error.mapper

import com.koflox.designsystem.text.UiText
import com.koflox.error.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class DefaultErrorMessageMapper(
    private val dispatcherDefault: CoroutineDispatcher,
) : ErrorMessageMapper {

    override suspend fun map(error: Throwable): UiText = withContext(dispatcherDefault) {
        UiText.Resource(R.string.error_not_handled)
    }
}
