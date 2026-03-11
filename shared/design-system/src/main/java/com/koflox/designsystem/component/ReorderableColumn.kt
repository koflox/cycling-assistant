package com.koflox.designsystem.component

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.zIndex

private const val DRAGGED_ITEM_SCALE = 1.03f

@Composable
fun <T> ReorderableColumn(
    items: List<T>,
    key: (T) -> Any,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: T, index: Int, dragModifier: Modifier) -> Unit,
) {
    val dragState = rememberReorderDragState()
    val currentOnReorder by rememberUpdatedState(onReorder)
    val currentItems by rememberUpdatedState(items)
    val currentKeySelector by rememberUpdatedState(key)
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val itemKey = key(item)
            key(itemKey) {
                val isDragged = dragState.draggedKey == itemKey
                val dragModifier = Modifier.pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { dragState.startDrag(itemKey) },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragState.handleDrag(
                                dragAmountY = dragAmount.y,
                                items = currentItems,
                                keySelector = currentKeySelector,
                                onReorder = currentOnReorder,
                            )
                        },
                        onDragEnd = { dragState.endDrag() },
                        onDragCancel = { dragState.endDrag() },
                    )
                }
                Box(
                    modifier = Modifier
                        .onGloballyPositioned {
                            dragState.itemHeights[itemKey] = it.size.height.toFloat()
                        }
                        .then(
                            if (isDragged) {
                                Modifier
                                    .zIndex(1f)
                                    .graphicsLayer {
                                        translationY = dragState.dragOffset
                                        scaleX = DRAGGED_ITEM_SCALE
                                        scaleY = DRAGGED_ITEM_SCALE
                                    }
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    itemContent(item, index, dragModifier)
                }
            }
        }
    }
}

@Composable
private fun rememberReorderDragState(): ReorderDragState {
    val view = LocalView.current
    return remember { ReorderDragState(view) }
}

private class ReorderDragState(private val view: View) {
    var draggedKey by mutableStateOf<Any?>(null)
    var dragOffset by mutableFloatStateOf(0f)
    val itemHeights = mutableStateMapOf<Any, Float>()

    fun startDrag(key: Any) {
        draggedKey = key
        dragOffset = 0f
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun endDrag() {
        draggedKey = null
        dragOffset = 0f
    }

    fun <T> handleDrag(
        dragAmountY: Float,
        items: List<T>,
        keySelector: (T) -> Any,
        onReorder: (Int, Int) -> Unit,
    ) {
        dragOffset += dragAmountY
        val currentDraggedKey = draggedKey
        if (currentDraggedKey != null) {
            val idx = items.indexOfFirst { keySelector(it) == currentDraggedKey }
            if (idx >= 0) {
                checkSwap(idx, items.lastIndex, dragOffset, items, keySelector, itemHeights)?.let { swap ->
                    onReorder(swap.fromIndex, swap.toIndex)
                    dragOffset += swap.offsetAdjustment
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
        }
    }
}

private data class SwapResult(
    val fromIndex: Int,
    val toIndex: Int,
    val offsetAdjustment: Float,
)

private fun <T> checkSwap(
    idx: Int,
    lastIndex: Int,
    dragOffset: Float,
    freshItems: List<T>,
    currentKeySelector: (T) -> Any,
    itemHeights: Map<Any, Float>,
): SwapResult? {
    val result = when {
        dragOffset > 0 && idx < lastIndex -> {
            val nextKey = currentKeySelector(freshItems[idx + 1])
            val nextHeight = itemHeights[nextKey] ?: 0f
            if (dragOffset > nextHeight / 2) SwapResult(idx, idx + 1, -nextHeight) else null
        }
        dragOffset < 0 && idx > 0 -> {
            val prevKey = currentKeySelector(freshItems[idx - 1])
            val prevHeight = itemHeights[prevKey] ?: 0f
            if (-dragOffset > prevHeight / 2) SwapResult(idx, idx - 1, prevHeight) else null
        }
        else -> null
    }
    return result
}
