package org.xuan.dynamis.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class WeatherForecast(
    val observedAt: LocalDateTime,
    val timeZone: String,
    val temperature: Double,
    val temperatureUnit: String,
    val weatherCode: Int?,
    val todayHigh: Double?,
    val todayLow: Double?,
    val hourly: List<HourlyForecast> = emptyList(),
    val dailyForecasts: List<DailyForecast> = emptyList(),
)

data class HourlyForecast(
    val time: LocalDateTime,
    val temperature: Double,
    val weatherCode: Int?,
)

data class DailyForecast(
    val date: LocalDate,
    val high: Double?,
    val low: Double?,
    val weatherCode: Int?,
    val precipitationProbability: Int?,
)
