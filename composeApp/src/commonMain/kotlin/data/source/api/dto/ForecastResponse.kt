package data.source.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    val timezone: String? = null,
    @SerialName("current_units") val currentUnits: CurrentUnits? = null,
    val current: Current? = null,
    val daily: Daily? = null,
)

@Serializable
data class CurrentUnits(
    @SerialName("temperature_2m") val temperature: String? = null,
)

@Serializable
data class Current(
    val time: String? = null,
    @SerialName("temperature_2m") val temperature: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
)

@Serializable
data class Daily(
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m_max") val high: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min") val low: List<Double?> = emptyList(),
)
