package data.source.api

import data.source.api.dto.ForecastResponse
import kotlinx.coroutines.flow.Flow

interface WeatherApi {
    fun getWeather(latitude: Double, longitude: Double): Flow<Result<ForecastResponse>>
}
