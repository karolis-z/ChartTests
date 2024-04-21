package com.example.testchartsapp.ui.screens.canvaschart.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun BarChart(
    chartData: BarChartData,
    onSelectBar: (BarItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedBar: BarItem? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = selectedBar?.id) {
        onSelectBar(selectedBar)
    }

    val bars: SnapshotStateList<Pair<BarItem, Float>> = remember(chartData.bars) {
        mutableStateListOf<Pair<BarItem, Float>>().apply {
            addAll(chartData.bars.map { it to 0f})
        }
    }
    val barHeightAnimationValues = bars.map { (_, height) ->
        animateFloatAsState(
            targetValue = height,
            animationSpec = tween(durationMillis = 500),
            label = "Bar height animation",
        )
    }
    val barAlphaAnimationValues = bars.map { (bar, _) ->
        animateFloatAsState(
            targetValue = when {
                selectedBar == null -> 1f
                selectedBar?.id == bar.id -> 1f
                else -> 0.2f
            },
            animationSpec = tween(durationMillis = 300),
            label = "Bar alpha animation",
        )
    }

    val barPositions = remember { ArrayList<ClosedFloatingPointRange<Float>>() }

    Canvas(
        modifier = modifier.pointerInput(bars) {
            detectTapGestures(
                onTap = { tapOffset ->
                    val tappedIndex = barPositions
                        .indexOfFirst { it.contains(tapOffset.x) }
                        .takeIf { it >= 0 }
                    tappedIndex?.let {
                        selectedBar =
                            (if (selectedBar?.id == bars[it].first.id) null else bars[it].first)
                    }
                }
            )
        },
    ) {
        val barWidth = this.size.width / bars.size / 2
        val padding = barWidth / 2

        var x = 0f

        if (barPositions.size != bars.size) { barPositions.clear() }

        bars.forEachIndexed { index, (barItem, _) ->
            val barHeight = this.size.height * barItem.heightFraction

            bars[index] = bars[index].copy(second = barHeight)

            if (barPositions.size != bars.size) {
                barPositions.add(x..(x + barWidth + padding * 2))
            }
            drawBar(
                x = x + padding,
                y = size.height - barHeightAnimationValues[index].value,
                width = barWidth,
                height = barHeightAnimationValues[index].value,
                color = barItem.color,
                alpha = barAlphaAnimationValues[index].value
//                alpha = when {
//                    selectedBar == null -> 1f
//                    selectedBar?.id == barItem.id -> 1f
//                    else -> 0.2f
//                }
            )
            x += barWidth + padding * 2
        }
    }
}

private fun DrawScope.drawBar(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: Color,
    alpha: Float = 1f,
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(25f, 25f),
        alpha = alpha,
    )
}

@Immutable
data class BarChartData(
    val bars: List<BarItem>,
//    val selectedBarColor: Color,
)


@Immutable
data class BarItem(
    val id: Long,
    val heightFraction: Float,
    val color: Color,
)
