package com.koflox.nutrition.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.koflox.designsystem.theme.Spacing
import com.koflox.nutrition.R
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@Composable
fun NutritionSettingsSectionRoute(
    modifier: Modifier = Modifier,
) {
    val viewModel: NutritionSettingsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    NutritionSettingsSectionContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
private fun NutritionSettingsSectionContent(
    uiState: NutritionSettingsUiState,
    onEvent: (NutritionSettingsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.nutrition_settings_enabled),
                style = MaterialTheme.typography.bodyLarge,
            )
            Checkbox(
                checked = uiState.isEnabled,
                onCheckedChange = { onEvent(NutritionSettingsUiEvent.EnabledChanged(it)) },
            )
        }
        if (uiState.isEnabled) {
            IntervalSlider(
                intervalMinutes = uiState.intervalMinutes,
                minIntervalMinutes = uiState.minIntervalMinutes,
                maxIntervalMinutes = uiState.maxIntervalMinutes,
                intervalStepMinutes = uiState.intervalStepMinutes,
                onIntervalChanged = { onEvent(NutritionSettingsUiEvent.IntervalChanged(it)) },
            )
        }
    }
}

@Composable
private fun IntervalSlider(
    intervalMinutes: Int,
    minIntervalMinutes: Int,
    maxIntervalMinutes: Int,
    intervalStepMinutes: Int,
    onIntervalChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.nutrition_settings_interval_label, intervalMinutes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = intervalMinutes.toFloat(),
            onValueChange = { value ->
                val snappedValue = snapToStep(value, intervalStepMinutes, minIntervalMinutes, maxIntervalMinutes)
                onIntervalChanged(snappedValue)
            },
            valueRange = minIntervalMinutes.toFloat()..maxIntervalMinutes.toFloat(),
            steps = ((maxIntervalMinutes - minIntervalMinutes) / intervalStepMinutes) - 1,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun snapToStep(value: Float, step: Int, min: Int, max: Int): Int {
    val snapped = ((value - min) / step).roundToInt() * step + min
    return snapped.coerceIn(min, max)
}
