package com.koflox.cyclingassistant.presentation.destinations.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val MIN_DISTANCE = 5f
private const val MAX_DISTANCE = 30f

@Composable
internal fun RouteSlider(
    distanceKm: Double,
    onDistanceChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "One way route length",
                style = MaterialTheme.typography.labelMedium,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${distanceKm.toInt()} km",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Slider(
                value = distanceKm.toFloat(),
                onValueChange = { onDistanceChanged(it.toDouble()) },
                valueRange = MIN_DISTANCE..MAX_DISTANCE,
                modifier = Modifier.widthIn(min = 250.dp),
            )
        }
    }
}
