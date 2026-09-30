package org.xuan.dynamis.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.xuan.dynamis.resources.Res
import org.xuan.dynamis.resources.current_location
import org.xuan.dynamis.resources.high_temperature
import org.xuan.dynamis.resources.loading_weather
import org.xuan.dynamis.resources.location_permission_denied
import org.xuan.dynamis.resources.location_permission_denied_forever
import org.xuan.dynamis.resources.location_unavailable
import org.xuan.dynamis.resources.low_temperature
import org.xuan.dynamis.resources.refresh_weather
import org.xuan.dynamis.resources.retry
import org.xuan.dynamis.resources.weather_unavailable
import org.jetbrains.compose.resources.stringResource
import org.xuan.dynamis.ui.theme.DynamisTheme

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background),
            ),
        ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 600.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(32.dp))
                IconButton(onClick = onRetry, enabled = state is HomeUiState.Success) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.refresh_weather))
                }
            }
            when (state) {
                HomeUiState.Loading -> {
                    CircularProgressIndicator(Modifier.padding(16.dp))
                    Text(stringResource(Res.string.loading_weather))
                }
                is HomeUiState.Error -> {
                    val message = when (state.reason) {
                        HomeError.PermissionDenied -> Res.string.location_permission_denied
                        HomeError.PermissionDeniedForever -> Res.string.location_permission_denied_forever
                        HomeError.LocationUnavailable -> Res.string.location_unavailable
                        HomeError.WeatherUnavailable -> Res.string.weather_unavailable
                    }
                    Text(stringResource(message), textAlign = TextAlign.Center)
                    Button(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
                }
                is HomeUiState.Success -> Unit
            }
            AnimatedVisibility(
                visible = state is HomeUiState.Success,
                enter = slideInVertically(tween(500)) { it } + fadeIn(tween(500)),
            ) {
                if (state is HomeUiState.Success) WeatherContent(state.weather)
            }
        }
    }
}

@Composable
private fun WeatherContent(weather: WeatherUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            weather.locality.ifBlank { stringResource(Res.string.current_location) },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(weather.time, style = MaterialTheme.typography.labelMedium)
        Text(weather.currentTemperature, style = MaterialTheme.typography.displayLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            TemperatureReading(
                weather.todayHigh, Icons.Default.KeyboardArrowUp,
                stringResource(Res.string.high_temperature, weather.todayHigh),
            )
            TemperatureReading(
                weather.todayLow, Icons.Default.KeyboardArrowDown,
                stringResource(Res.string.low_temperature, weather.todayLow),
            )
        }
    }
}

@Composable
private fun TemperatureReading(value: String, icon: ImageVector, description: String) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null)
        Text(value)
    }
}

private val PreviewWeather = WeatherUiState(
    locality = "San Francisco, California", time = "14:30",
    currentTemperature = "18°C", todayHigh = "22°C", todayLow = "12°C",
)

@Preview(name = "Success - Light", showBackground = true)
@Composable
private fun HomeScreenSuccessLightPreview() {
    PreviewHomeScreen(HomeUiState.Success(PreviewWeather))
}

@Preview(name = "Success - Dark", showBackground = true)
@Composable
private fun HomeScreenSuccessDarkPreview() {
    PreviewHomeScreen(HomeUiState.Success(PreviewWeather), darkTheme = true)
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

@Composable
private fun PreviewHomeScreen(state: HomeUiState, darkTheme: Boolean = false) {
    DynamisTheme(darkTheme = darkTheme) { HomeScreen(state, onRetry = {}) }
}
