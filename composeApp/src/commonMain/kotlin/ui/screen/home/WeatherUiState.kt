package ui.screen.home

import androidx.compose.ui.graphics.Color

data class WeatherUiState(
    val locality: String = "",
    val time: String = "00:00",
    val currentTemperature: String = "0",
    val currentWeatherCode: Int = 0,
    val todayHigh: String = "0",
    val todayLow: String = "0",
    val backgroundGradient: Pair<Color, Color> = ClearDay
)
