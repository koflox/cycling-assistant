package com.koflox.poi.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.koflox.poi.R
import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.presentation.buttons.ActivePoiButtonsUiState
import com.koflox.poi.presentation.buttons.ActivePoiButtonsViewModel
import com.koflox.poi.presentation.mapper.label

@Composable
fun PoiSettingsSectionRoute(
    onNavigateToPoiSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivePoiButtonsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    when (val state = uiState) {
        is ActivePoiButtonsUiState.Loading -> Unit
        is ActivePoiButtonsUiState.Content -> {
            PoiSettingsSectionContent(
                selectedPois = state.selectedPois,
                onNavigateToPoiSelection = onNavigateToPoiSelection,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun PoiSettingsSectionContent(
    selectedPois: List<PoiType>,
    onNavigateToPoiSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToPoiSelection),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.poi_settings_label),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (selectedPois.isNotEmpty()) {
                val labels = selectedPois.map { it.label() }
                Text(
                    text = labels.joinToString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
