package com.example.testchartsapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@Composable
fun CanvasChartScreen(
    prices: List<Price> = fakeChartData
) {
    val originalItems = remember { prices.toMutableStateList() }
    val tempItemForRemovalTest = remember { originalItems[4].copy(time = originalItems[4].time.plusMinutes(30)) }

    val bars = remember(originalItems.toList()) {
        val highestPrice = originalItems.maxOf { it.price }
        originalItems.map {
            BarItem(
                id = it.time.toEpochSecond(ZoneOffset.UTC),
                heightFraction = (it.price / highestPrice).toFloat(),
            )
        }
    }

    var selectedBar: BarItem? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp)
        ,
    ) {
        BarChart(
            chartData = BarChartData(
                bars = bars,
            ),
            onSelectBar = { selectedBar = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(all = 4.dp)
                .border(0.5.dp, Color.Green),
        )

        ScreenContent(
            originalItems = originalItems,
            onAddRemoveItemClick = {
                if (bars.size <= 24) {
                    originalItems.add(3, tempItemForRemovalTest)
                } else {
                    originalItems.removeAt(3)
                }
            },
            onRandomize5thItemClick = {
                originalItems[4] = originalItems[4].copy(price = Random.nextDouble(1.00, 4.5))
            },
            selectedBar = selectedBar,
            selectedIndex = selectedBar?.let { bars.indexOf(it) }
        )
    }
}

@Composable
private fun BarChart(
    chartData: BarChartData,
    onSelectBar: (BarItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val barColor = Color(0xFF80C362)
    val selectedBarColor = Color(0xFF545A63)

    var selectedBar: BarItem? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = selectedBar?.id) {
        onSelectBar(selectedBar)
    }

    val bars: SnapshotStateList<Pair<BarItem, Float>> = remember(chartData.bars) {
        mutableStateListOf<Pair<BarItem, Float>>().apply {
            addAll(chartData.bars.map { it to 0f})
        }
    }
    val animationValues = bars.map { (_, height) ->
        animateFloatAsState(
            targetValue = height,
            animationSpec = tween(durationMillis = 1000),
            label = "Bar height animation",
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
                y = size.height - animationValues[index].value,
                width = barWidth,
                height = animationValues[index].value,
                color = if (barItem.id == selectedBar?.id) selectedBarColor else barColor,
            )
            x += barWidth + padding * 2
        }
    }
}

@Immutable
data class BarChartData(
    val bars: List<BarItem>,
)

@Composable
private fun ScreenContent(
    originalItems: SnapshotStateList<Price>,
    onAddRemoveItemClick: () -> Unit,
    onRandomize5thItemClick: () -> Unit,
    selectedBar: BarItem?,
    selectedIndex: Int?,
) {
    Spacer(modifier = Modifier.height(50.dp))
    Button(onClick = onAddRemoveItemClick) {
        Text(text = "Add/remove item")
    }
    Spacer(modifier = Modifier.height(20.dp))
    Button(onClick = onRandomize5thItemClick) {
        Text(text = "Randomize 5th item")
    }

    fun getSelectedText(): String {
        return selectedIndex?.let { index ->
            "#$index: " + originalItems[index].toString() + "\nHeight: ${selectedBar?.heightFraction}"
        } ?: "None selected"
    }

    Text(text = getSelectedText())
}

private fun DrawScope.drawBar(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: Color,
) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = CornerRadius(25f, 25f),
    )
}

@Immutable
data class BarItem(
    val id: Long,
    val heightFraction: Float,
)

@Immutable
data class Price(
    val time: LocalDateTime,
    val price: Double,
) {
    override fun toString(): String {
       return "Price: $price. at ${time.format(DateTimeFormatter.ISO_DATE)} ${time.format(DateTimeFormatter.ofPattern("H:mm"))}"
    }
}

private val fakeChartData = listOf(
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(0), 1.00),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(1), 1.80),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(2), 1.90),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(3), 0.5),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(4), 3.06),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(5), 3.5),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(6), 0.75),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(7), 2.56),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(8), 2.66),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(9), 2.89),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(10), 1.98),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(11), 1.25),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(12), 1.46),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(13), 1.05),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(14), 0.87),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(15), 0.56),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(16), 0.00),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(17), 0.25),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(18), 0.1),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(19), 1.67),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(20), 1.88),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(21), 2.25),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(22), 2.45),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(23), 2.69),
)