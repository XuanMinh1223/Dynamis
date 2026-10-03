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
    moonPhase = moonPhaseFraction(observedAt, timeZone),
    todayHigh = todayHigh.format(temperatureUnit),
    todayLow = todayLow.format(temperatureUnit),
    hourly = hourly.map { forecast ->
        HourlyWeatherUiState(
            time = "${forecast.time.hour.toString().padStart(2, '0')}:00",
            hour = forecast.time.hour,
            temperature = forecast.temperature.format(temperatureUnit),
            weatherCode = forecast.weatherCode,
        )
    },
    dailyForecasts = dailyForecasts.map { forecast ->
        DailyWeatherUiState(
            date = forecast.date,
            isToday = forecast.date == observedAt.date,
            highTemperature = forecast.high.format(temperatureUnit),
            lowTemperature = forecast.low.format(temperatureUnit),
            weatherCode = forecast.weatherCode,
            precipitationProbability = forecast.precipitationProbability,
        )
    },
)

private fun Double?.format(unit: String): String = this?.let { "${it.roundToInt()}$unit" } ?: "—"
