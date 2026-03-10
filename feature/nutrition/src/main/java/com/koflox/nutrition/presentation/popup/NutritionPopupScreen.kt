package com.koflox.nutrition.presentation.popup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.koflox.concurrent.CurrentTimeProvider
import com.koflox.designsystem.theme.Elevation
import com.koflox.designsystem.theme.Spacing
import com.koflox.nutrition.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface NutritionPopupEntryPoint {
    fun currentTimeProvider(): CurrentTimeProvider
    @com.koflox.di.DefaultDispatcher fun defaultDispatcher(): CoroutineDispatcher
}

@Composable
fun NutritionPopupRoute(
    suggestionTimeMs: Long,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val entryPoint = EntryPointAccessors.fromApplication(context, NutritionPopupEntryPoint::class.java)
    val currentTimeProvider = entryPoint.currentTimeProvider()
    val dispatcherDefault = entryPoint.defaultDispatcher()
    val stateHolder = remember(suggestionTimeMs) {
        NutritionPopupStateHolder(
            suggestionTimeMs = suggestionTimeMs,
            currentTimeProvider = currentTimeProvider,
            dispatcherDefault = dispatcherDefault,
        )
    }
    DisposableEffect(suggestionTimeMs) {
        onDispose { stateHolder.dispose() }
    }
    val uiState by stateHolder.uiState.collectAsState()
    NutritionPopupContent(
        uiState = uiState,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@Composable
private fun NutritionPopupContent(
    uiState: NutritionPopupUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        NutritionPopupUiState.Hidden -> Unit
        is NutritionPopupUiState.Visible -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.Prominent),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Large, vertical = Spacing.Medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.nutrition_popup_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = stringResource(R.string.nutrition_popup_timer, uiState.elapsedTimerFormatted),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = Spacing.Small),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.nutrition_popup_dismiss),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}
