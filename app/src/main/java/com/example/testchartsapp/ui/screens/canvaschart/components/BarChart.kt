package com.example.testchartsapp.ui.screens.canvaschart.components

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import com.example.testchartsapp.ui.screens.canvaschart.components.BarChartDefaults.BarAndContentVerticalSpacing
import com.example.testchartsapp.ui.screens.canvaschart.components.BarChartDefaults.BarLabelPadding
import com.example.testchartsapp.ui.screens.canvaschart.components.BarChartDefaults.IconVerticalPadding
import com.example.testchartsapp.ui.screens.canvaschart.components.BarChartDefaults.MinimumSelectedBarIndicatorLineLength

@Composable
fun BarChart(
    chartData: BarChartData,
    onSelectBar: (BarItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    val bars: SnapshotStateList<Pair<BarItem, Float>> = remember(chartData.bars) {
        mutableStateListOf<Pair<BarItem, Float>>().apply {
            addAll(chartData.bars.map { it to 0f})
        }
    }
    val barPositions = remember { ArrayList<ClosedFloatingPointRange<Float>>() }

    var touchX: Float? by remember { mutableStateOf(null) }
    val selectedBar: BarItem? by remember {
        derivedStateOf {
            val x = touchX
            when {
                x == null -> null
                x < 0f -> null
                else -> {
                    val index = barPositions
                        .indexOfFirst { it.contains(x) }
                        .takeIf { it >= 0 }
                    index?.let { bars[it].first }
                }
            }
        }
    }

    LaunchedEffect(key1 = selectedBar?.id) {
        onSelectBar(selectedBar)
        if (selectedBar != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val icons = bars.map { (bar, _) ->
        bar.icon?.let {
            BarPainter(
                painter = painterResource(id = it.iconResId),
                color = it.color,
            )
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

    val textMeasurer = rememberTextMeasurer()

    var textDrawableWidth by remember { mutableFloatStateOf(0f) }

    val maxLabelHeight = remember(chartData, textDrawableWidth) {
        if (textDrawableWidth >= 0f) {
            chartData.bars.maxOf { bar ->
                textMeasurer.measure(
                    text = bar.label,
                    style = chartData.labelTextStyle,
                    constraints = Constraints(maxWidth = textDrawableWidth.toInt())
                ).size.height
            }
        } else 0
    }

    fun BarItem.isSelected(): Boolean {
        return this.id == selectedBar?.id
    }

    Canvas(
        modifier = modifier.pointerInput(bars) {
            detectDragGestures(
                onDragCancel = { touchX = null },
                onDragEnd = { touchX = null },
                onDragStart = { touchX = it.x }
            ) { _, dragAmount -> touchX = touchX?.plus(dragAmount.x) }
        },
    ) {
        val labelPadding = BarLabelPadding.toPx()
        textDrawableWidth = size.width - labelPadding * 2

        val barWidth = this.size.width / bars.size / 2
        val padding = barWidth / 2

        val heightForLabel = maxLabelHeight + labelPadding * 2

        if (barPositions.size != bars.size) { barPositions.clear() }

        val iconSize = chartData.iconSize.toPx()
        val iconSpace = iconSize + IconVerticalPadding.toPx() * 2

        var x = 0f
        bars.forEachIndexed { index, (barItem, _) ->
            inset(0f, heightForLabel + iconSpace, 0f, 0f) {
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
            val xBarMiddle = (xEnd - x) / 2 + x

            val contentAboveBarY = barHeightAnimationValues[index].value + BarAndContentVerticalSpacing.toPx()

            val icon = icons[index]
            if (icon != null && !barItem.isSelected()) {
                drawIcon(
                    x = xBarMiddle - iconSize / 2,
                    y = size.height - contentAboveBarY - iconSize,
                    icon = icon,
                    size = iconSize,
                )
            }

            if (barItem.isSelected()) {
                val textResult = textMeasurer.measure(
                    text = barItem.label,
                    style = chartData.labelTextStyle,
                    constraints = Constraints(maxWidth = textDrawableWidth.toInt()),
                )
                val labelXStart = (xBarMiddle - textResult.size.center.x).coerceIn(
                    minimumValue = 0f + labelPadding,
                    maximumValue = size.width - labelPadding - textResult.size.width
                )
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        x = labelXStart,
                        y = labelPadding,
                    ),
                )
                chartData.lineSettings?.let { line ->
                    drawSelectedIndicatorLine(
                        x = xBarMiddle,
                        startY = labelPadding * 2f + textResult.size.height,
                        endY = size.height - contentAboveBarY,
                        lineSettings = line,
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

private fun DrawScope.drawSelectedIndicatorLine(
    x: Float,
    startY: Float,
    endY: Float,
    lineSettings: SelectedBarIndicatorLine,
) {
    if (endY >= startY + MinimumSelectedBarIndicatorLineLength.roundToPx()) {
        drawLine(
            color = lineSettings.color,
            start = Offset(x, startY),
            end = Offset(x, endY),
            strokeWidth = lineSettings.strokeWidth.toPx(),
            cap = lineSettings.cap,
            pathEffect = lineSettings.pathEffect,
            alpha = lineSettings.alpha,
            colorFilter = lineSettings.colorFilter,
            blendMode = lineSettings.blendMode,
        )
    }
}

private fun DrawScope.drawIcon(
    x: Float,
    y: Float,
    icon: BarPainter,
    size: Float,
) {
    with(icon.painter) {
        translate(x, y) {
            draw(
                size = Size(size, size),
                colorFilter = ColorFilter.tint(icon.color)
            )
        }
    }
}

private object BarChartDefaults {
    val BarLabelPadding = 2.dp
    val MinimumSelectedBarIndicatorLineLength = 3.dp
    val BarAndContentVerticalSpacing = 3.dp
    val IconVerticalPadding = 2.dp
}

@Immutable
data class BarChartData(
    val bars: List<BarItem>,
    val labelTextStyle: TextStyle,
    val lineSettings: SelectedBarIndicatorLine?,
    val iconSize: Dp = 16.dp,
)

@Immutable
data class SelectedBarIndicatorLine(
    val color: Color,
    val strokeWidth: Dp = 2.dp,
    val cap: StrokeCap = StrokeCap.Round,
    val pathEffect: PathEffect? = null,
    @FloatRange(from = 0.0, to = 1.0) val alpha: Float = 1.0f,
    val colorFilter: ColorFilter? = null,
    val blendMode: BlendMode = DefaultBlendMode,
)


@Immutable
data class BarItem(
    val id: Long,
    val heightFraction: Float,
    val color: Color,
    val label: String,
    val icon: Icon? = null,
) {
    @Immutable
    data class Icon(
        @DrawableRes val iconResId: Int,
        val color: Color,
    )
}

@Immutable
data class BarPainter(
    val painter: Painter,
    val color: Color,
)