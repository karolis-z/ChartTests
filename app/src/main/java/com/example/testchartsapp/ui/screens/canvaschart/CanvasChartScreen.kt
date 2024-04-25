package com.example.testchartsapp.ui.screens.canvaschart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.testchartsapp.ui.screens.canvaschart.components.BarChart
import com.example.testchartsapp.ui.screens.canvaschart.components.BarChartData
import com.example.testchartsapp.ui.screens.canvaschart.components.BarItem
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
                color = it.priceLevel.barColor(),
                label = it.time.format(DateTimeFormatter.ofPattern("H:mm")),
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
                labelTextStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                selectedLineColor = MaterialTheme.colorScheme.onSurface,
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
            selectedIndex = selectedBar?.let { bar ->
                bars.indexOfFirst { bar.id == selectedBar?.id }.takeIf { it >= 0 }
            }
        )
    }
}

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

private fun Price.PriceLevel.barColor(): Color {
    return when (this) {
        Price.PriceLevel.LOW -> Color(0xFF80C362)
        Price.PriceLevel.MEDIUM -> Color(0xFF545A63)
        Price.PriceLevel.HIGH -> Color(0xFF313842)
    }
}

@Immutable
data class Price(
    val time: LocalDateTime,
    val price: Double,
    val priceLevel: PriceLevel,
) {

    enum class PriceLevel {
        LOW,
        MEDIUM,
        HIGH,
    }
    override fun toString(): String {
       return "Price: $price. at ${time.format(DateTimeFormatter.ISO_DATE)} ${time.format(DateTimeFormatter.ofPattern("H:mm"))}"
    }
}

private val fakeChartData = listOf(
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(0), 1.00, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(1), 1.80, Price.PriceLevel.MEDIUM),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(2), 1.90, Price.PriceLevel.MEDIUM),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(3), 0.5, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(4), 3.06, Price.PriceLevel.HIGH),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(5), 3.5, Price.PriceLevel.HIGH),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(6), 0.75, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(7), 2.56, Price.PriceLevel.MEDIUM),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(8), 2.66, Price.PriceLevel.HIGH),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(9), 2.89, Price.PriceLevel.HIGH),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(10), 1.98, Price.PriceLevel.MEDIUM),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(11), 1.25, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(12), 1.46, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(13), 1.05, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(14), 0.87, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(15), 0.56, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(16), 0.00, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(17), 0.25, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(18), 0.1, Price.PriceLevel.LOW),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(19), 1.67, Price.PriceLevel.MEDIUM),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(20), 1.88, Price.PriceLevel.MEDIUM),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(21), 2.25, Price.PriceLevel.MEDIUM),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(22), 2.45, Price.PriceLevel.MEDIUM),
    Price(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(23), 2.69, Price.PriceLevel.HIGH),
)