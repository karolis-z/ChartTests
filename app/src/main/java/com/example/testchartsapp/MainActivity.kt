package com.example.testchartsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import com.example.testchartsapp.ui.screens.CanvasChartScreen
import com.example.testchartsapp.ui.screens.HomeScreen
import com.example.testchartsapp.ui.screens.ScrollableCanvasChartScreen
import com.example.testchartsapp.ui.theme.TestChartsAppTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestChartsAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(
        startDestination = "home_screen",
        navController = navController
    ) {
        composable("home_screen") {
            TestChartsAppTheme {
                HomeScreen(
                    onNavigateToCanvasChart = { navController.navigate("canvas_chart_screen") },
                    onNavigateToScrollableCanvasChart = { navController.navigate("scrollable_canvas_chart") },
                )
            }
        }
        composable("canvas_chart_screen") {
            TestChartsAppTheme {
                CanvasChartScreen()
            }
        }
        composable("scrollable_canvas_chart") {
            TestChartsAppTheme {
                ScrollableCanvasChartScreen()
            }
        }
    }
}


@Composable
fun VicoChart(
    modifier: Modifier = Modifier,
) {
    val data = remember {
        getChartDataForVicoChart()
    }
    val marker = rememberMarker()
    Chart(
        chart = columnChart(persistentMarkers = remember(marker) { mapOf(PERSISTENT_MARKER_X to marker) }),
        chartModelProducer = ChartEntryModelProducer(
            entryCollections = listOf(data)
        ),
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(guideline = null),
        marker = marker,
        runInitialAnimation = true,
        chartScrollSpec = rememberChartScrollSpec(),

        )
}

private const val COLOR_1_CODE = 0xffa485e0
private const val PERSISTENT_MARKER_X = 10f

@Composable
fun YChartsChart(
    modifier: Modifier = Modifier,
) {
    val chartData = remember { getChartData() }
    BarChart(
        modifier = modifier,
        barChartData = BarChartData(
            chartData = chartData,
        ),
    )
}

private fun getChartData(): List<BarData> {
    val data = mutableListOf<BarData>()
    repeat(50) {
        data.add(
            BarData(Point(x = it.toFloat(), y = Random.nextInt(0, 50).toFloat()))
        )
    }
    return data
}

private fun getChartDataForVicoChart(): List<ChartEntry> {
    val generator = RandomEntriesGenerator(
        xRange = 0..50,
        yRange = 0..15,
    )
//    val data = mutableListOf<ChartEntry>()
//    repeat(50) {
//        data.add(
//            ChartEntry()
//        )
//    }
    return generator.generateRandomEntries()
}

@Preview
@Composable
private fun AppPreview() {
    TestChartsAppTheme {
        App()
    }
}