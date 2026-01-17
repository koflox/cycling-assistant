package com.koflox.destinations.presentation.destinations.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.koflox.destinations.R

private const val MIN_DISTANCE = 5f
private const val MAX_DISTANCE = 30f
private const val TOLERANCE_FONT_SIZE_RATIO = 0.7f

@Composable
internal fun RouteSlider(
    distanceKm: Double,
    toleranceKm: Double,
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
                text = stringResource(R.string.one_way_distance_title),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))

            val headlineStyle = MaterialTheme.typography.headlineSmall
            val toleranceFontSize = headlineStyle.fontSize * TOLERANCE_FONT_SIZE_RATIO
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${distanceKm.toInt()} ")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.km_unit))
                    }
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = toleranceFontSize,
                            baselineShift = BaselineShift((1 - TOLERANCE_FONT_SIZE_RATIO) / 2),
                        ),
                    ) {
                        append(" (±$toleranceKm)")
                    }
                },
                style = headlineStyle,
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
