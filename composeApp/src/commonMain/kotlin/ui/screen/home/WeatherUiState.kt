package ui.screen.home

import androidx.compose.ui.graphics.Color

data class WeatherUiState(
    val locality: String = "",
    val time: String = "—",
    val currentTemperature: String = "—",
    val currentWeatherCode: Int = 0,
    val todayHigh: String = "—",
    val todayLow: String = "—",
    val backgroundGradient: Pair<Color, Color> = ClearDay
)
