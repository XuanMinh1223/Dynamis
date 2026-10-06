package org.xuan.dynamis.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.min

private val CodeSamples =
    listOf(
        SymbolSample("0  Clear", 0),
        SymbolSample("1  Mainly clear", 1),
        SymbolSample("2  Partly cloudy", 2),
        SymbolSample("3  Overcast", 3),
        SymbolSample("45  Fog", 45),
        SymbolSample("48  Rime fog", 48),
        SymbolSample("51  Light drizzle", 51),
        SymbolSample("53  Drizzle", 53),
        SymbolSample("55  Heavy drizzle", 55),
        SymbolSample("56  Freezing drizzle", 56),
        SymbolSample("57  Dense freezing drizzle", 57),
        SymbolSample("61  Light rain", 61),
        SymbolSample("63  Rain", 63),
        SymbolSample("65  Heavy rain", 65),
        SymbolSample("66  Freezing rain", 66),
        SymbolSample("67  Heavy freezing rain", 67),
        SymbolSample("71  Light snow", 71),
        SymbolSample("73  Snow", 73),
        SymbolSample("75  Heavy snow", 75),
        SymbolSample("77  Snow grains", 77),
        SymbolSample("80  Light rain showers", 80),
        SymbolSample("81  Rain showers", 81),
        SymbolSample("82  Heavy rain showers", 82),
        SymbolSample("85  Snow showers", 85),
        SymbolSample("86  Heavy snow showers", 86),
        SymbolSample("95  Thunderstorm", 95),
        SymbolSample("96  Thunder and hail", 96),
        SymbolSample("99  Heavy hailstorm", 99),
    )

@Preview(name = "Paper symbols - Sky and fog", widthDp = 420, heightDp = 720)
@Composable
private fun PaperSkyAndFogPreview() {
    SymbolGallery(CodeSamples.filter { it.code in listOf(0, 1, 2, 3, 45, 48) }, TimeOfDay.Day)
}

@Preview(name = "Paper symbols - Drizzle and rain", widthDp = 420, heightDp = 1120)
@Composable
private fun PaperRainPreview() {
    SymbolGallery(CodeSamples.filter { it.code in 51..67 }, TimeOfDay.Day)
}

@Preview(name = "Paper symbols - Snow and showers", widthDp = 420, heightDp = 1120)
@Composable
private fun PaperSnowAndShowersPreview() {
    SymbolGallery(CodeSamples.filter { it.code in 71..86 }, TimeOfDay.Day)
}

@Preview(name = "Paper snowflake cutouts", widthDp = 420, heightDp = 420)
@Composable
private fun PaperSnowflakeCutoutsPreview() {
    DynamisTheme {
        WeatherMeshGradientBackground(WeatherPattern.Snowfall, TimeOfDay.Day) {
            Canvas(Modifier.fillMaxSize()) {
                val cellWidth = size.width / 3f
                val cellHeight = size.height / 3f
                for (design in 0 until 9) {
                    val x = cellWidth * (design % 3 + 0.5f)
                    val y = cellHeight * (design / 3 + 0.5f)
                    drawCutPaperSnowflake(
                        point = { px, py -> Offset(px, py) },
                        x = x,
                        y = y,
                        radius = min(cellWidth, cellHeight) * 0.31f,
                        design = design,
                        unit = 1f,
                    )
                }
            }
        }
    }
}

@Preview(name = "Paper symbols - Thunderstorms", widthDp = 420, heightDp = 470)
@Composable
private fun PaperThunderstormPreview() {
    SymbolGallery(CodeSamples.filter { it.code in 95..99 }, TimeOfDay.Day)
}

@Preview(name = "Paper symbols - Night", widthDp = 420, heightDp = 920)
@Composable
private fun PaperWeatherSymbolsNightPreview() {
    SymbolGallery(
        CodeSamples.filter { it.code in listOf(0, 2, 3, 48, 63, 73, 95, 99) },
        TimeOfDay.Night,
    )
}

@Preview(name = "Paper moon phases", widthDp = 420, heightDp = 920)
@Composable
private fun PaperMoonPhasesPreview() {
    val phases =
        listOf(
            "New" to 0f,
            "Waxing crescent" to 0.125f,
            "First quarter" to 0.25f,
            "Waxing gibbous" to 0.375f,
            "Full" to 0.5f,
            "Waning gibbous" to 0.625f,
            "Last quarter" to 0.75f,
            "Waning crescent" to 0.875f,
        )
    SymbolGallery(
        phases.map { (name, phase) -> SymbolSample(name, 0, phase) },
        TimeOfDay.Night,
    )
}

@Composable
private fun SymbolGallery(
    samples: List<SymbolSample>,
    timeOfDay: TimeOfDay,
) {
    DynamisTheme(darkTheme = timeOfDay == TimeOfDay.Night) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            samples.chunked(2).forEach { rowSamples ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowSamples.forEach { sample ->
                        Box(Modifier.weight(1f).height(210.dp).clip(RoundedCornerShape(18.dp))) {
                            WeatherMeshGradientBackground(
                                WeatherPattern.fromWeatherCode(sample.code),
                                timeOfDay,
                            ) {
                                Box(Modifier.fillMaxSize()) {
                                    WeatherPaperSymbol(
                                        sample.code,
                                        timeOfDay,
                                        sample.moonPhase,
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                    Text(
                                        sample.name,
                                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class SymbolSample(
    val name: String,
    val code: Int,
    val moonPhase: Float = 0.25f,
)
