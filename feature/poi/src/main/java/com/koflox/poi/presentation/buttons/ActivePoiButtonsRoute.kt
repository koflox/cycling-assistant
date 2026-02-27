package com.koflox.poi.presentation.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.koflox.designsystem.component.DebouncedOutlinedButton
import com.koflox.designsystem.theme.Spacing
import com.koflox.poi.presentation.mapper.label
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActivePoiButtonsRoute(
    onPoiClicked: (query: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivePoiButtonsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    when (val state = uiState) {
        is ActivePoiButtonsUiState.Loading -> Unit
        is ActivePoiButtonsUiState.Content -> {
            ActivePoiButtonsContent(
                selectedPois = state.selectedPois,
                onPoiClicked = onPoiClicked,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ActivePoiButtonsContent(
    selectedPois: List<com.koflox.poi.domain.model.PoiType>,
    onPoiClicked: (query: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val poiButtonColors = ButtonDefaults.outlinedButtonColors(
        containerColor = MaterialTheme.colorScheme.surface,
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
    ) {
        selectedPois.forEach { poiType ->
            val poiLabel = poiType.label()
            DebouncedOutlinedButton(
                onClick = { onPoiClicked(poiLabel) },
                modifier = Modifier.weight(1f),
                colors = poiButtonColors,
            ) {
                Text(poiLabel)
            }
        }
    }
}
