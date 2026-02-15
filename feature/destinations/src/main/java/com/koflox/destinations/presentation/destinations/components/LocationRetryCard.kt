package com.koflox.destinations.presentation.destinations.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.koflox.designsystem.component.ActionCard
import com.koflox.destinations.R

@Composable
internal fun LocationRetryCard(
    onEnableLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ActionCard(
        message = stringResource(R.string.location_disabled_message),
        buttonLabel = stringResource(R.string.location_disabled_enable),
        onButtonClick = onEnableLocationClick,
        modifier = modifier,
    )
}
