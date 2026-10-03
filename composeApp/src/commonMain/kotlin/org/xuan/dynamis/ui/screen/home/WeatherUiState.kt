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
