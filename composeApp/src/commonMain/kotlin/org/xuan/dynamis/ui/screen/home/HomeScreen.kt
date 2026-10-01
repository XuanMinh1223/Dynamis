package org.xuan.dynamis.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.xuan.dynamis.resources.Res
import org.xuan.dynamis.resources.current_location
import org.xuan.dynamis.resources.high_temperature
import org.xuan.dynamis.resources.hourly_forecast_item
import org.xuan.dynamis.resources.loading_weather
import org.xuan.dynamis.resources.location_permission_denied
import org.xuan.dynamis.resources.location_permission_denied_forever
import org.xuan.dynamis.resources.location_unavailable
import org.xuan.dynamis.resources.low_temperature
import org.xuan.dynamis.resources.next_24_hours
import org.xuan.dynamis.resources.refresh_weather
import org.xuan.dynamis.resources.retry
import org.xuan.dynamis.resources.weather_unavailable
import org.xuan.dynamis.ui.theme.LocalWeatherColorPalette
import org.xuan.dynamis.ui.theme.TimeOfDay
import org.xuan.dynamis.ui.theme.WeatherMeshGradientBackground
import org.xuan.dynamis.ui.theme.WeatherPaperSymbol
import org.xuan.dynamis.ui.theme.WeatherPattern

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val weather = (state as? HomeUiState.Success)?.weather
    WeatherMeshGradientBackground(
        pattern = weather?.weatherPattern ?: WeatherPattern.Unknown,
        timeOfDay = weather?.timeOfDay ?: TimeOfDay.Day,
        modifier = modifier,
    ) {
        // Keep content clear of camera cutouts and system bars while the background fills the screen.
        Box(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(32.dp))
                    IconButton(
                        onClick = onRetry,
                        enabled = state is HomeUiState.Success,
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                contentColor = LocalContentColor.current,
                                disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f),
                            ),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.refresh_weather))
                    }
                }
                when (state) {
                    HomeUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp),
                            color = LocalContentColor.current,
                        )
                        Text(stringResource(Res.string.loading_weather))
                    }

                    is HomeUiState.Error -> {
                        val message =
                            when (state.reason) {
                                HomeError.PermissionDenied -> Res.string.location_permission_denied
                                HomeError.PermissionDeniedForever -> Res.string.location_permission_denied_forever
                                HomeError.LocationUnavailable -> Res.string.location_unavailable
                                HomeError.WeatherUnavailable -> Res.string.weather_unavailable
                            }
                        Text(stringResource(message), textAlign = TextAlign.Center)
                        Button(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
                    }

                    is HomeUiState.Success -> {
                        Unit
                    }
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
        WeatherPaperSymbol(weather.currentWeatherCode, weather.timeOfDay, weather.moonPhase)
        Text(weather.currentTemperature, style = MaterialTheme.typography.displayLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TemperatureReading(
                weather.todayHigh,
                Icons.Default.KeyboardArrowUp,
                stringResource(Res.string.high_temperature, weather.todayHigh),
            )
            Text(
                text = weather.weatherPattern.displayName,
                style = MaterialTheme.typography.bodyMedium,
            )
            TemperatureReading(
                weather.todayLow,
                Icons.Default.KeyboardArrowDown,
                stringResource(Res.string.low_temperature, weather.todayLow),
            )
        }
        if (weather.hourly.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.next_24_hours),
                    style = MaterialTheme.typography.titleSmall,
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(
                        items = weather.hourly,
                        key = { index, item -> "$index-${item.time}" },
                    ) { _, hour ->
                        HourlyWeatherCard(hour, weather.moonPhase)
                    }
                }
            }
        }
    }
}

@Composable
private fun HourlyWeatherCard(
    hour: HourlyWeatherUiState,
    moonPhase: Float,
) {
    val palette = LocalWeatherColorPalette.current
    val paper = lerp(Color(0xFFFFF8E9), palette.weatherSecondary, 0.28f)
    val ink = Color(0xFF203342)
    val weatherName = WeatherPattern.fromWeatherCode(hour.weatherCode).displayName
    val description =
        stringResource(
            Res.string.hourly_forecast_item,
            weatherName,
            hour.time,
            hour.temperature,
        )

    Surface(
        modifier =
            Modifier
                .width(82.dp)
                .height(130.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = description
                },
        shape = RoundedCornerShape(18.dp),
        color = paper,
        contentColor = ink,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, palette.shadow.copy(alpha = 0.22f)),
    ) {
        Box {
            PaperStockTexture(palette.shadow)
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(hour.time, style = MaterialTheme.typography.labelMedium)
                WeatherPaperSymbol(
                    weatherCode = hour.weatherCode,
                    timeOfDay = hour.timeOfDay,
                    moonPhase = moonPhase,
                    modifier = Modifier.size(width = 70.dp, height = 62.dp),
                )
                Text(hour.temperature, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun PaperStockTexture(shadow: Color) {
    Canvas(Modifier.fillMaxSize()) {
        val stroke = 0.7.dp.toPx()
        for (fiber in 0 until 24) {
            val x = ((fiber * 47) % 101) / 100f * size.width
            val y = ((fiber * 73) % 97) / 97f * size.height
            val length = (8 + fiber % 13).dp.toPx()
            drawLine(
                color = shadow.copy(alpha = if (fiber % 3 == 0) 0.10f else 0.045f),
                start = Offset(x, y),
                end = Offset((x + length).coerceAtMost(size.width), y + (fiber % 3 - 1) * stroke),
                strokeWidth = stroke,
            )
        }
    }
}

@Composable
private fun TemperatureReading(
    value: String,
    icon: ImageVector,
    description: String,
) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null)
        Text(value)
    }
}
