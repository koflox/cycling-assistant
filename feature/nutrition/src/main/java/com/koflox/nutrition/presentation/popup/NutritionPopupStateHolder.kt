package com.koflox.nutrition.presentation.popup

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

internal class NutritionPopupStateHolder(
    private val suggestionTimeMs: Long,
    dispatcherDefault: CoroutineDispatcher,
) {

    companion object {
        private const val TIMER_UPDATE_INTERVAL_MS = 1000L
    }

    private val scope = CoroutineScope(dispatcherDefault)

    private val _uiState = MutableStateFlow<NutritionPopupUiState>(NutritionPopupUiState.Hidden)
    val uiState: StateFlow<NutritionPopupUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        scope.launch {
            updateElapsedTime()
            while (isActive) {
                delay(TIMER_UPDATE_INTERVAL_MS)
                updateElapsedTime()
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }

    private fun updateElapsedTime() {
        val elapsedMs = System.currentTimeMillis() - suggestionTimeMs
        _uiState.value = NutritionPopupUiState.Visible(
            elapsedTimerFormatted = formatElapsedTime(elapsedMs),
        )
    }

    private fun formatElapsedTime(elapsedMs: Long): String {
        val totalSeconds = (elapsedMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
