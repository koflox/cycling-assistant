package com.koflox.poi.presentation.buttons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.koflox.designsystem.component.DebouncedOutlinedButton
import com.koflox.designsystem.component.LocalizedDialog
import com.koflox.designsystem.theme.Spacing
import com.koflox.poi.R
import com.koflox.poi.domain.model.PoiType
import com.koflox.poi.presentation.mapper.label

@Composable
internal fun MorePoisDialog(
    unselectedPois: List<PoiType>,
    onPoiClicked: (query: String) -> Unit,
    onNavigateToPoiSelection: () -> Unit,
    onDismiss: () -> Unit,
) {
    LocalizedDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(Spacing.Large)) {
                Text(
                    text = stringResource(R.string.poi_more_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(bottom = Spacing.Medium)
                        .align(Alignment.CenterHorizontally),
                )
                val poiButtonColors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                    verticalItemSpacing = Spacing.Small,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(unselectedPois) { poiType ->
                        val poiLabel = poiType.label()
                        DebouncedOutlinedButton(
                            onClick = { onPoiClicked(poiLabel) },
                            colors = poiButtonColors,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = poiLabel,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                ReselectText(
                    onClick = onNavigateToPoiSelection,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = Spacing.Medium),
                )
            }
        }
    }
}

@Composable
private fun ReselectText(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefix = stringResource(R.string.poi_more_dialog_reselect_prefix)
    val action = stringResource(R.string.poi_more_dialog_reselect_action)
    val suffix = stringResource(R.string.poi_more_dialog_reselect_suffix)
    val annotatedText = buildAnnotatedString {
        append(prefix)
        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
            append(action)
        }
        append(suffix)
    }
    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.clickable(onClick = onClick),
    )
}
