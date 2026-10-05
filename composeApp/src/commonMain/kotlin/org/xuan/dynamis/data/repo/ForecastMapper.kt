package org.xuan.dynamis.data.repo

import co.touchlab.kermit.Logger
import org.xuan.dynamis.data.source.api.dto.ForecastResponse
import org.xuan.dynamis.domain.model.DailyForecast
import org.xuan.dynamis.domain.model.HourlyForecast
import org.xuan.dynamis.domain.model.WeatherForecast
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

class InvalidForecastException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)

fun ForecastResponse.toWeatherForecast(): WeatherForecast {
    val current = current ?: throw InvalidForecastException("Current weather is missing")
    val timestamp = current.time ?: throw InvalidForecastException("Observation time is missing")
    val observedAt = try {
        // Open-Meteo returns wall-clock time in the forecast location's timezone.
        LocalDateTime.parse(timestamp)
    } catch (cause: IllegalArgumentException) {
        throw InvalidForecastException("Observation time is invalid", cause)
    }
    val timeZone = timezone ?: throw InvalidForecastException("Forecast timezone is missing")
    try {
        TimeZone.of(timeZone)
    } catch (cause: IllegalArgumentException) {
        throw InvalidForecastException("Forecast timezone is invalid", cause)
    }
    val temperature = current.temperature?.takeIf { it.isFinite() }
        ?: throw InvalidForecastException("Current temperature is missing or invalid")
    val unit = currentUnits?.temperature?.takeIf { it.isNotBlank() }
        ?: throw InvalidForecastException("Temperature unit is missing")
    val dateIndex = daily?.time.orEmpty().indexOf(observedAt.date.toString())
    val hourlyForecast = hourly?.let { hourlyData ->
        hourlyData.time.mapIndexedNotNull { index, timestamp ->
            val forecastTime = try {
                LocalDateTime.parse(timestamp)
            } catch (cause: IllegalArgumentException) {
                Logger.withTag("ForecastMapper").w(cause) { "Skipping hourly entry $index with invalid time '$timestamp'" }
                null
            }
            val forecastTemperature = hourlyData.temperature.getOrNull(index)?.takeIf { it.isFinite() }
            if (forecastTime == null || forecastTime < observedAt || forecastTemperature == null) {
                null
            } else {
                HourlyForecast(
                    time = forecastTime,
                    temperature = forecastTemperature,
                    weatherCode = hourlyData.weatherCode.getOrNull(index),
                )
            }
        }
    }.orEmpty().take(24)
    val dailyForecasts = daily?.let { dailyData ->
        dailyData.time.mapIndexedNotNull { index, dateText ->
            val date = try {
                LocalDate.parse(dateText)
            } catch (cause: IllegalArgumentException) {
                Logger.withTag("ForecastMapper").w(cause) { "Skipping daily entry $index with invalid date '$dateText'" }
                null
            } ?: return@mapIndexedNotNull null
            DailyForecast(
                date = date,
                high = dailyData.high.getOrNull(index)?.takeIf { it.isFinite() },
                low = dailyData.low.getOrNull(index)?.takeIf { it.isFinite() },
                weatherCode = dailyData.weatherCode.getOrNull(index),
                precipitationProbability =
                    dailyData.precipitationProbability.getOrNull(index)?.takeIf { it in 0..100 },
            )
        }
    }.orEmpty().take(16)

    return WeatherForecast(
        observedAt = observedAt,
        timeZone = timeZone,
        temperature = temperature,
        temperatureUnit = unit,
        weatherCode = current.weatherCode,
        todayHigh = daily?.high?.getOrNull(dateIndex)?.takeIf { it.isFinite() },
        todayLow = daily?.low?.getOrNull(dateIndex)?.takeIf { it.isFinite() },
        hourly = hourlyForecast,
        dailyForecasts = dailyForecasts,
    )
}
