package com.example.testchartsapp

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.testchartsapp.ui.theme.TestChartsAppTheme
import kotlin.random.Random

private const val TAG = "CANVAS_TEST"
private const val SCROLL_TAG = "SCROLL_STATE"

@Composable
fun ScrollableCanvas(
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenWidthInPx = with(LocalDensity.current) { screenWidth.dp.toPx() }

    val chartMaxYValue = 100f
    val fakeData = remember { getFakeChartData() }

    var scrollOffset by remember { mutableStateOf(0f) }
    val maxScrollOffset by remember { mutableStateOf(3000f) } // TODO: Need actual calculation

    val scrollState = rememberScrollableState { delta ->
        Log.d(SCROLL_TAG, "ScrollableCanvas: delta = $delta")
        scrollOffset -= delta
        scrollOffset = checkAndGetMaxScrollOffset(scrollOffset, maxScrollOffset)
        delta
    }
//    val scrollState = rememberScrollState()

//    LaunchedEffect(key1 = scrollState, block = {
//        Log.d(SCROLL_TAG, "Scroll value: ${scrollState.isScrollInProgress}")
//    })

    LaunchedEffect(key1 = true, block = {
        Log.d(TAG, "Screen width is $screenWidth dp = $screenWidthInPx in px.")
    })
    LaunchedEffect(key1 = fakeData, block = {
        Log.d(TAG, "fakeData = $fakeData")
    })

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 30.dp, horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .scrollable(
                    state = scrollState,
                    orientation = Orientation.Horizontal,
                    enabled = true,
                    reverseDirection = false,
                    flingBehavior = null,
                    interactionSource = null
                )
                .fillMaxSize()
                .border(2.dp, Color.Green)
                .clipToBounds(),
        ) {
            Log.d(TAG, "Canvas width: ${this.size.width} ")
            val topPadding = 20.dp.toPx()
            val bottomPadding = 20.dp.toPx()
            val spaceBetweenBars = 10.dp.toPx()
            val areaHeight = this.size.height - topPadding - bottomPadding
            val barWidth = 16.dp.toPx()
            fakeData.forEachIndexed { index, myChartData ->
                val barHeight = areaHeight * myChartData.y / chartMaxYValue
                val topLeftOffset = getDrawOffset(
                    barIndex = index,
                    spaceBetweenBars = spaceBetweenBars,
                    barWidth = barWidth,
                    scrollOffset = scrollOffset,
                    areaHeight = areaHeight,
                    topPadding = topPadding,
                    yValue = myChartData.y,
                    maxYValue = chartMaxYValue
                )
                drawRect(
                    color = myChartData.barColor,
                    topLeft = topLeftOffset,
                    size = Size(barWidth, barHeight),
                )
            }
//            drawUnderScrollMask(24.dp, 24.dp, Color.DarkGray)
        }
    }
}

private fun getDrawOffset(
    barIndex: Int,
    spaceBetweenBars: Float,
    barWidth: Float,
    scrollOffset: Float,
    areaHeight: Float,
    topPadding: Float,
    yValue: Float,
    maxYValue: Float,
): Offset {
    val barHeight = areaHeight * yValue / maxYValue
    val yOffsetFromChartAreaTop = areaHeight - barHeight
    val x = (barIndex * (barWidth + spaceBetweenBars) - scrollOffset)
    val y = topPadding + yOffsetFromChartAreaTop
    return Offset(x, y)
}

private fun getFakeChartData(): List<MyChartData> {
    val data = mutableListOf<MyChartData>()
    repeat(50) {
        data.add(MyChartData(it, Random.nextFloat() * 100, Color(Random.nextInt())))
    }
    return data
}

data class MyChartData(
    val x: Int,
    val y: Float,
    val barColor: Color,
)

/**
 *
 * DrawScope.drawUnderScrollMask extension method used  for drawing a rectangular mask to make graph scrollable under the YAxis.
 * @param columnWidth : Width of the rectangular mask here width of Y Axis is used.
 * @param paddingRight : Padding given at the end of the graph.
 * @param bgColor : Background of the rectangular mask.
 */
internal fun DrawScope.drawUnderScrollMask(paddingLeft: Dp, paddingRight: Dp, bgColor: Color) {
    // Draw column to make graph look scrollable under Yaxis
    drawRect(
        bgColor, Offset(0f, 0f), Size(paddingLeft.toPx(), size.height)
    )
    // Draw right padding
    drawRect(
        bgColor,
        Offset(size.width - paddingRight.toPx(), 0f),
        Size(paddingRight.toPx(), size.height)
    )
}

/**
 * Returns the scroll state within the start and computed max scrollOffset & filters invalid scroll states.
 * @param currentScrollOffset: Current scroll offset when user trying to scroll the canvas.
 * @param computedMaxScrollOffset: Maximum calculated scroll offset for given data set.
 */
fun checkAndGetMaxScrollOffset(currentScrollOffset: Float, computedMaxScrollOffset: Float): Float {
    return when {
        currentScrollOffset < 0f -> 0f
        currentScrollOffset > computedMaxScrollOffset -> computedMaxScrollOffset
        else -> currentScrollOffset
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CanvasPreview() {
    TestChartsAppTheme {
        ScrollableCanvas()
    }
}