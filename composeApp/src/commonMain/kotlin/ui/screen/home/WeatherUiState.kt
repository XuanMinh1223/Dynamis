package ui.screen.home

data class WeatherUiState(
    val locality: String = "",
    val time: String = "—",
    val currentTemperature: String = "—",
    val currentWeatherCode: Int = 0,
    val todayHigh: String = "—",
    val todayLow: String = "—",
)
