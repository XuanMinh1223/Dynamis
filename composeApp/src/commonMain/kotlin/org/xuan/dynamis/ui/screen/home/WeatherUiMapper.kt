package org.xuan.dynamis.ui.screen.home

import org.xuan.dynamis.domain.model.WeatherForecast
import org.xuan.dynamis.ui.theme.TimeOfDay
import kotlin.math.roundToInt

fun WeatherForecast.toUiState(locality: String? = null): WeatherUiState = WeatherUiState(
    locality = locality.orEmpty(),
    time = "${observedAt.hour.toString().padStart(2, '0')}:${observedAt.minute.toString().padStart(2, '0')}",
    currentTemperature = temperature.format(temperatureUnit),
    currentWeatherCode = weatherCode,
    timeOfDay = TimeOfDay.fromHour(observedAt.hour),
    todayHigh = todayHigh.format(temperatureUnit),
    todayLow = todayLow.format(temperatureUnit),
)

private fun Double?.format(unit: String): String = this?.let { "${it.roundToInt()}$unit" } ?: "—"
