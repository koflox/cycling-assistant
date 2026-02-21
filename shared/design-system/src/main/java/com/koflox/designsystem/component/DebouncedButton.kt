package com.koflox.designsystem.component

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

private const val DEBOUNCE_INTERVAL_MS = 400L

@Composable
fun DebouncedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable () -> Unit,
) {
    var lastClickTimeMs by remember { mutableLongStateOf(0L) }
    Button(
        onClick = {
            val now = System.currentTimeMillis()
            if (now - lastClickTimeMs >= DEBOUNCE_INTERVAL_MS) {
                lastClickTimeMs = now
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        content = { content() },
    )
}

@Composable
fun DebouncedOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    var lastClickTimeMs by remember { mutableLongStateOf(0L) }
    OutlinedButton(
        onClick = {
            val now = System.currentTimeMillis()
            if (now - lastClickTimeMs >= DEBOUNCE_INTERVAL_MS) {
                lastClickTimeMs = now
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        content = { content() },
    )
}
