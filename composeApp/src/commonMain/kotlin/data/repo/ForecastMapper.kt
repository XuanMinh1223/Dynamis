package data.repo

import data.source.api.dto.ForecastResponse
import domain.model.WeatherForecast
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

    return WeatherForecast(
        observedAt = observedAt,
        timeZone = timeZone,
        temperature = temperature,
        temperatureUnit = unit,
        weatherCode = current.weatherCode,
        todayHigh = daily?.high?.getOrNull(dateIndex)?.takeIf { it.isFinite() },
        todayLow = daily?.low?.getOrNull(dateIndex)?.takeIf { it.isFinite() },
    )
}
