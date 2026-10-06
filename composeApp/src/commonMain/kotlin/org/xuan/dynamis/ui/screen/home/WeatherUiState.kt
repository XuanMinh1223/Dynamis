package org.xuan.dynamis.ui.screen.home

import kotlinx.datetime.LocalDate
import org.xuan.dynamis.ui.theme.TimeOfDay
import org.xuan.dynamis.ui.theme.WeatherPattern

data class WeatherUiState(
    val locality: String = "",
    val time: String = "—",
    val currentTemperature: String = "—",
    val currentWeatherCode: Int? = null,
    val timeOfDay: TimeOfDay = TimeOfDay.Day,
    val moonPhase: Float = 0.5f,
    val todayHigh: String = "—",
    val todayLow: String = "—",
    val hourly: List<HourlyWeatherUiState> = emptyList(),
    val dailyForecasts: List<DailyWeatherUiState> = emptyList(),
) {
    val weatherPattern: WeatherPattern
        get() = WeatherPattern.fromWeatherCode(currentWeatherCode)
}

data class DailyWeatherUiState(
    val date: LocalDate,
    val isToday: Boolean,
    val highTemperature: String,
    val lowTemperature: String,
    val weatherCode: Int?,
    val precipitationProbability: Int?,
)

data class HourlyWeatherUiState(
    val time: String,
    val hour: Int,
    val temperature: String,
    val weatherCode: Int?,
) {
    val timeOfDay: TimeOfDay
        get() = TimeOfDay.fromHour(hour)
}

/**
 * Stand-in data for the loading state. Never visible: it only gives the skeleton the same
 * layout (text widths, card counts) as typical loaded data, so the crossfade moves nothing.
 */
internal val PlaceholderWeather =
    WeatherUiState(
        locality = "Placeholder City",
        time = "00:00",
        currentTemperature = "00°C",
        currentWeatherCode = 2,
        todayHigh = "00°C",
        todayLow = "00°C",
        hourly = List(24) { HourlyWeatherUiState("00:00", hour = 12, temperature = "00°C", weatherCode = 2) },
        dailyForecasts =
            List(16) {
                DailyWeatherUiState(
                    date = LocalDate(2000, 1, 1),
                    isToday = false,
                    highTemperature = "00°C",
                    lowTemperature = "00°C",
                    weatherCode = 2,
                    precipitationProbability = null,
                )
            },
    )
