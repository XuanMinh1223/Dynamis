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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import org.jetbrains.compose.resources.stringResource
import org.xuan.dynamis.resources.Res
import org.xuan.dynamis.resources.current_location
import org.xuan.dynamis.resources.daily_forecast_accessibility
import org.xuan.dynamis.resources.daily_forecast_rain_accessibility
import org.xuan.dynamis.resources.daily_forecast_snow_accessibility
import org.xuan.dynamis.resources.high_temperature
import org.xuan.dynamis.resources.hourly_forecast_item
import org.xuan.dynamis.resources.loading_weather
import org.xuan.dynamis.resources.location_permission_denied
import org.xuan.dynamis.resources.location_permission_denied_forever
import org.xuan.dynamis.resources.location_unavailable
import org.xuan.dynamis.resources.low_temperature
import org.xuan.dynamis.resources.next_16_days
import org.xuan.dynamis.resources.next_24_hours
import org.xuan.dynamis.resources.precipitation_chance
import org.xuan.dynamis.resources.refresh_weather
import org.xuan.dynamis.resources.retry
import org.xuan.dynamis.resources.today
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
    radarContent: @Composable () -> Unit = {},
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
                    if (state is HomeUiState.Success) WeatherContent(state.weather, radarContent)
                }
            }
        }
    }
}

@Composable
private fun WeatherContent(weather: WeatherUiState, radarContent: @Composable () -> Unit) {
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
        if (weather.dailyForecasts.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.next_16_days),
                    style = MaterialTheme.typography.titleSmall,
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(
                        items = weather.dailyForecasts,
                        key = { index, item -> "$index-${item.date}" },
                    ) { _, day ->
                        DailyWeatherCard(day, weather.moonPhase)
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(top = 12.dp)) { radarContent() }
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
private fun DailyWeatherCard(
    day: DailyWeatherUiState,
    moonPhase: Float,
) {
    val palette = LocalWeatherColorPalette.current
    val paper = lerp(Color(0xFFFFF8E9), palette.weatherSecondary, 0.28f)
    val ink = Color(0xFF203342)
    val dateLabel =
        if (day.isToday) {
            stringResource(Res.string.today)
        } else {
            "${day.date.month.ordinal + 1}/${day.date.day}"
        }
    val weatherName = WeatherPattern.fromWeatherCode(day.weatherCode).displayName
    val precipitation = day.precipitationProbability?.takeIf { it >= 30 }
    val snow = day.weatherCode in 71..77 || day.weatherCode == 85 || day.weatherCode == 86
    val description =
        if (precipitation == null) {
            stringResource(
                Res.string.daily_forecast_accessibility,
                dateLabel,
                weatherName,
                day.highTemperature,
                day.lowTemperature,
            )
        } else if (snow) {
            stringResource(
                Res.string.daily_forecast_snow_accessibility,
                dateLabel,
                weatherName,
                day.highTemperature,
                day.lowTemperature,
                precipitation,
            )
        } else {
            stringResource(
                Res.string.daily_forecast_rain_accessibility,
                dateLabel,
                weatherName,
                day.highTemperature,
                day.lowTemperature,
                precipitation,
            )
        }

    Surface(
        modifier =
            Modifier
                .width(96.dp)
                .height(158.dp)
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
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(dateLabel, style = MaterialTheme.typography.labelMedium)
                WeatherPaperSymbol(
                    weatherCode = day.weatherCode,
                    timeOfDay = TimeOfDay.Day,
                    moonPhase = moonPhase,
                    modifier = Modifier.size(width = 82.dp, height = 70.dp),
                )
                Box(
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (precipitation != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            PrecipitationIcon(snow)
                            Text(
                                text = stringResource(Res.string.precipitation_chance, precipitation),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF376F85),
                            )
                        }
                    }
                }
                Text("↑ ${day.highTemperature}", style = MaterialTheme.typography.labelSmall)
                Text("↓ ${day.lowTemperature}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PrecipitationIcon(snow: Boolean) {
    Canvas(Modifier.size(12.dp)) {
        val tint = Color(0xFF376F85)
        val centerX = size.width / 2f
        if (snow) {
            val center = Offset(centerX, size.height / 2f)
            val armLength = size.minDimension * 0.44f
            for (arm in 0 until 3) {
                val angle = arm * PI.toFloat() / 3f
                val offset = Offset(cos(angle) * armLength, sin(angle) * armLength)
                drawLine(
                    color = tint,
                    start = center - offset,
                    end = center + offset,
                    strokeWidth = 1.4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        } else {
            val drop = Path().apply {
                moveTo(centerX, 0f)
                cubicTo(size.width * 0.34f, size.height * 0.38f, size.width * 0.1f, size.height * 0.57f,
                    size.width * 0.1f, size.height * 0.72f)
                cubicTo(size.width * 0.1f, size.height * 0.91f, size.width * 0.28f, size.height,
                    centerX, size.height)
                cubicTo(size.width * 0.72f, size.height, size.width * 0.9f, size.height * 0.91f,
                    size.width * 0.9f, size.height * 0.72f)
                cubicTo(size.width * 0.9f, size.height * 0.57f, size.width * 0.66f, size.height * 0.38f,
                    centerX, 0f)
                close()
            }
            drawPath(drop, tint)
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
