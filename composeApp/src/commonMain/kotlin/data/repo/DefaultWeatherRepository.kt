package data.repo

import data.source.api.WeatherApi
import domain.WeatherRepository
import domain.model.WeatherForecast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DefaultWeatherRepository(
    private val api: WeatherApi,
) : WeatherRepository {
    override fun getWeather(latitude: Double, longitude: Double): Flow<Result<WeatherForecast>> =
        api.getWeather(latitude, longitude).map { result ->
            result.mapCatching { it.toWeatherForecast() }
        }.catch { e ->
            emit(Result.failure(e))
        }
}
