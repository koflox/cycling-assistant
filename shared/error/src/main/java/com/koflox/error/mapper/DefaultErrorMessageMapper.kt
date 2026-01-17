package com.koflox.error.mapper

import android.content.Context
import com.koflox.error.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class DefaultErrorMessageMapper(
    private val context: Context,
    private val dispatcherDefault: CoroutineDispatcher,
) : ErrorMessageMapper {

    override suspend fun map(error: Throwable): String = withContext(dispatcherDefault) {
        context.getString(R.string.error_not_handled)
    }
}
