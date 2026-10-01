package org.xuan.dynamis.ui.screen.home

import org.xuan.dynamis.ui.theme.TimeOfDay
import org.xuan.dynamis.ui.theme.WeatherPattern

data class WeatherUiState(
    val locality: String = "",
    val time: String = "—",
    val currentTemperature: String = "—",
    val currentWeatherCode: Int? = null,
    val timeOfDay: TimeOfDay = TimeOfDay.Day,
    val todayHigh: String = "—",
    val todayLow: String = "—",
) {
    val weatherPattern: WeatherPattern
        get() = WeatherPattern.fromWeatherCode(currentWeatherCode)
}
