package com.example.testchartsapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun CanvasChartScreen(

) {

    val chartData = remember {
        val highestPrice = fakeChartData.maxOf { it.price }
        fakeChartData.map {
            BarItem(
                height = (it.price / highestPrice).toFloat(),
            )
        }
    }.toMutableStateList()
    val barColor = Color(0xFF80C362)
    val selectedBarColor = Color(0xFF545A63)

    var selectedBar: BarItem? by remember { mutableStateOf(null) }

    val barPositions = remember {
        ArrayList<ClosedFloatingPointRange<Float>>()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF3F3F3)),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(all = 12.dp)
                .border(0.5.dp, Color.Green)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val tappedIndex = barPositions
                                .indexOfFirst { it.contains(tapOffset.x) }
                                .takeIf { it >= 0 }
                            tappedIndex?.let {
                                selectedBar = (if (selectedBar == chartData[it]) null else chartData[it])
                            }
                            Log.d("TESTING", "CanvasChartScreen: selectedBar = $selectedBar. tapOffset = $tapOffset")
                            barPositions.forEachIndexed { index, closedFloatingPointRange ->
                                Log.d("TESTING", "CanvasChartScreen: #$index. range = $closedFloatingPointRange")
                            }
                        }
                    )
                },
        ) {
            val barWidth = this.size.width / chartData.size / 2
            val padding = barWidth / 2

            var x = 0f
            if (barPositions.size != chartData.size) {
                barPositions.clear()
            }
            chartData.forEachIndexed { index, barItem ->
                val barHeight = this.size.height * barItem.height
                val xStartOfBar = x + padding
                if (barPositions.size != chartData.size) {
                    val tappableRange = x..(x + barWidth + padding * 2)
                    barPositions.add(tappableRange)
                }
                drawRoundRect(
                    color = if (barItem == selectedBar) selectedBarColor else barColor,
                    topLeft = Offset(xStartOfBar, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(25f, 25f),
                )
                x += barWidth + padding * 2
            }
        }
    }
}

@Immutable
data class BarItem(
    val height: Float,
)

@Immutable
data class Price(
    val time: LocalDateTime,
    val price: Double,
)

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