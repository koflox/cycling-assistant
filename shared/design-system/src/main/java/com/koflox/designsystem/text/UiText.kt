package com.koflox.designsystem.text

import android.content.Context
import androidx.annotation.StringRes

/**
 * Represents a text value that can be resolved with a [Context].
 *
 * Use this instead of raw strings in ViewModels and mappers to defer string resolution
 * to the composable layer, ensuring the app-selected locale is applied.
 */
sealed interface UiText {
    /**
     * A text backed by a string resource.
     *
     * @param resId the string resource ID
     * @param args format arguments for the string resource
     */
    data class Resource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText
}

/**
 * Resolves this [UiText] to a [String] using the given [context].
 *
 * In composables, pass `LocalContext.current` to get the locale-aware context.
 */
@Suppress("SpreadOperator")
fun UiText.resolve(context: Context): String = when (this) {
    is UiText.Resource -> {
        if (args.isEmpty()) {
            context.getString(resId)
        } else {
            context.getString(resId, *args.toTypedArray())
        }
    }
}
