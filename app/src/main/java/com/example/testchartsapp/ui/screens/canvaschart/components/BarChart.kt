package com.example.testchartsapp.ui.screens.canvaschart.components

import android.util.Log
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import com.example.testchartsapp.ui.screens.canvaschart.components.BarChartDefaults.BarLabelPadding

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
    val textMeasurer = rememberTextMeasurer()

    // TODO: Need a better way, because this does not account for padding within canvas. This could measure as 1 line but would actually be 2 lined text
    val maxLabelHeight = remember(chartData) {
        chartData.bars.maxOf {
            textMeasurer.measure(it.label, chartData.labelTextStyle).size.height
        }
    }

    fun BarItem.isSelected(): Boolean {
        return this.id == selectedBar?.id
    }

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

        val labelPadding = BarLabelPadding.toPx()
        val heightForLabel = maxLabelHeight + labelPadding * 2

        if (barPositions.size != bars.size) { barPositions.clear() }

        bars.forEachIndexed { index, (barItem, _) ->
            inset(0f, heightForLabel, 0f, 0f) {
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
                )
            }

            val xEnd = x + barWidth + padding * 2

            if (barItem.isSelected()) {
                val xBarMiddle = (xEnd - x) / 2 + x

                val textResult = textMeasurer.measure(
                    text = barItem.label,
                    style = chartData.labelTextStyle,
                    constraints = Constraints(maxWidth = (size.width - labelPadding * 2).toInt()),
                )
                Log.d("DRAWTEXT", "BarChart: xstart = $x. xEnd = $xEnd. text width = ${textResult.size.width}. text height = ${textResult.size.height}. drawable width = ${size.width}. label padding = $labelPadding")
                val labelXStart = (xBarMiddle - textResult.size.center.x).coerceIn(0f + labelPadding, size.width - labelPadding - textResult.size.width)
//                val labelXEnd = size.width - (labelXStart + textMeasured.size.width)
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        x = labelXStart,
                        y = labelPadding,
                    ),
                )

                // TODO: End could calculate to be fore start. Need some checks before deciding to draw.
                // TODO: better variable naming 
                val startY = labelPadding * 2 + textResult.size.height
                val endY = size.height - barHeightAnimationValues[index].value - 3.dp.toPx()
                Log.d("TESTING", "BarChart: endY of line = $endY. start y = ${labelPadding * 2 + textResult.size.height}")
                // TODO: More complex logic needed? Need some buffer to decide to draw the line? Maybe at least 3dp length? 
                if (endY > startY) {
                    drawLine(
                        color = chartData.selectedLineColor,
                        start = Offset(
                            x = xBarMiddle,
                            y = labelPadding * 2 + textResult.size.height,
                        ),
                        end = Offset(
                            x = xBarMiddle,
                            y = size.height - barHeightAnimationValues[index].value - 3.dp.toPx(),
                        ),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }

            x = xEnd
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

private object BarChartDefaults {
    val BarLabelPadding = 1.dp
}

@Immutable
data class BarChartData(
    val bars: List<BarItem>,
    val labelTextStyle: TextStyle,
    // TODO: Something more extensive to cover other styling options?
    val selectedLineColor: Color,
)


@Immutable
data class BarItem(
    val id: Long,
    val heightFraction: Float,
    val color: Color,
    val label: String,
)