package org.xuan.dynamis.ui.screen.home

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.xuan.dynamis.ui.theme.DynamisTheme
import org.xuan.dynamis.ui.theme.TimeOfDay
import org.xuan.dynamis.ui.theme.WeatherMeshGradientBackground
import org.xuan.dynamis.ui.theme.WeatherPattern

private val PreviewWeather = WeatherUiState(
    locality = "San Francisco, California",
    time = "14:30",
    currentTemperature = "18°C",
    currentWeatherCode = 0,
    timeOfDay = TimeOfDay.Day,
    todayHigh = "22°C",
    todayLow = "12°C",
)

@Preview(name = "Success - Light", showBackground = true)
@Composable
private fun HomeScreenSuccessLightPreview() {
    PreviewHomeScreen(HomeUiState.Success(PreviewWeather))
}

@Preview(name = "Success - Dark", showBackground = true)
@Composable
private fun HomeScreenSuccessDarkPreview() {
    PreviewHomeScreen(
        HomeUiState.Success(PreviewWeather.copy(currentWeatherCode = 95, timeOfDay = TimeOfDay.Night)),
        darkTheme = true,
    )
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    PreviewHomeScreen(HomeUiState.Loading)
}

@Preview(name = "Error - Permission denied", showBackground = true)
@Composable
private fun HomeScreenPermissionDeniedPreview() {
    PreviewHomeScreen(HomeUiState.Error(HomeError.PermissionDenied))
}

@Preview(name = "Error - Permission denied forever", showBackground = true)
@Composable
private fun HomeScreenPermissionDeniedForeverPreview() {
    PreviewHomeScreen(HomeUiState.Error(HomeError.PermissionDeniedForever))
}

@Preview(name = "Error - Location unavailable", showBackground = true)
@Composable
private fun HomeScreenLocationUnavailablePreview() {
    PreviewHomeScreen(HomeUiState.Error(HomeError.LocationUnavailable))
}

@Preview(name = "Error - Weather unavailable", showBackground = true)
@Composable
private fun HomeScreenWeatherUnavailablePreview() {
    PreviewHomeScreen(HomeUiState.Error(HomeError.WeatherUnavailable))
}

@Preview(name = "Weather palettes - Dawn", widthDp = 420, heightDp = 960)
@Composable
private fun WeatherPaletteDawnPreview() {
    PreviewWeatherPaletteGallery(TimeOfDay.Dawn)
}

@Preview(name = "Weather palettes - Day", widthDp = 420, heightDp = 960)
@Composable
private fun WeatherPaletteDayPreview() {
    PreviewWeatherPaletteGallery(TimeOfDay.Day)
}

@Preview(name = "Weather palettes - Dusk", widthDp = 420, heightDp = 960)
@Composable
private fun WeatherPaletteDuskPreview() {
    PreviewWeatherPaletteGallery(TimeOfDay.Dusk)
}

@Preview(name = "Weather palettes - Night", widthDp = 420, heightDp = 960)
@Composable
private fun WeatherPaletteNightPreview() {
    PreviewWeatherPaletteGallery(TimeOfDay.Night)
}

/** Enable Interactive Mode to change targets or play a repeating sequence. */
@Preview(name = "Weather palettes - Animated transitions", widthDp = 420, heightDp = 720)
@Composable
private fun WeatherPaletteTransitionPreview() {
    var pattern by remember { mutableStateOf(WeatherPattern.ClearSky) }
    var timeOfDay by remember { mutableStateOf(TimeOfDay.Day) }
    var playing by remember { mutableStateOf(false) }

    LaunchedEffect(playing) {
        if (playing) {
            while (true) {
                delay(3_000)
                pattern = WeatherPattern.entries[(pattern.ordinal + 1) % WeatherPattern.entries.size]
                timeOfDay = TimeOfDay.entries[(timeOfDay.ordinal + 1) % TimeOfDay.entries.size]
            }
        }
    }

    DynamisTheme(darkTheme = false) {
        WeatherMeshGradientBackground(pattern = pattern, timeOfDay = timeOfDay) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(pattern.displayName(), style = MaterialTheme.typography.titleLarge)
                    Text(timeOfDay.name, style = MaterialTheme.typography.labelLarge)
                    Text("18°C", style = MaterialTheme.typography.displayLarge)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            pattern = WeatherPattern.entries[(pattern.ordinal + 1) % WeatherPattern.entries.size]
                        }) { Text("Next weather") }
                        Button(onClick = {
                            timeOfDay = TimeOfDay.entries[(timeOfDay.ordinal + 1) % TimeOfDay.entries.size]
                        }) { Text("Next time") }
                    }
                    Button(onClick = { playing = !playing }) {
                        Text(if (playing) "Pause transitions" else "Play transitions")
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewWeatherPaletteGallery(timeOfDay: TimeOfDay) {
    DynamisTheme(darkTheme = timeOfDay == TimeOfDay.Night) {
        val patterns = WeatherPattern.entries
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            patterns.chunked(2).forEach { rowPatterns ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowPatterns.forEach { pattern ->
                        Box(
                            modifier = Modifier.weight(1f).height(112.dp)
                                .clip(RoundedCornerShape(16.dp)),
                        ) {
                            WeatherMeshGradientBackground(
                                pattern = pattern,
                                timeOfDay = timeOfDay,
                            ) {
                                Text(
                                    text = pattern.displayName(),
                                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                                )
                            }
                        }
                    }
                    if (rowPatterns.size == 1) Box(Modifier.weight(1f).height(112.dp))
                }
            }
        }
    }
}

private fun WeatherPattern.displayName(): String = name
    .replace(Regex("([a-z])([A-Z])"), "\$1 \$2")

@Composable
private fun PreviewHomeScreen(state: HomeUiState, darkTheme: Boolean = false) {
    DynamisTheme(darkTheme = darkTheme) { HomeScreen(state, onRetry = {}) }
}
