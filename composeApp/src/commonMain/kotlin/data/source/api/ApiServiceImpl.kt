package data.source.api

import data.source.api.ApiConstants.ParameterNames.CURRENT
import data.source.api.ApiConstants.ParameterNames.DAILY
import data.source.api.ApiConstants.ParameterNames.HOURLY
import data.source.api.ApiConstants.ParameterNames.LATITUDE
import data.source.api.ApiConstants.ParameterNames.LONGITUDE
import data.source.api.ApiConstants.ParameterNames.PRECIPITATION_UNIT
import data.source.api.ApiConstants.ParameterNames.TEMPERATURE_UNIT
import data.source.api.ApiConstants.ParameterNames.TIMEZONE
import data.source.api.ApiConstants.ParameterNames.WIND_SPEED_UNIT
import data.source.api.ApiConstants.ParameterValues.CURRENT_VALUES
import data.source.api.ApiConstants.ParameterValues.DAILY_VALUES
import data.source.api.ApiConstants.ParameterValues.DEFAULT_PRECIPITATION_UNIT
import data.source.api.ApiConstants.ParameterValues.DEFAULT_TEMPERATURE_UNIT
import data.source.api.ApiConstants.ParameterValues.DEFAULT_TIMEZONE
import data.source.api.ApiConstants.ParameterValues.DEFAULT_WIND_SPEED_UNIT
import data.source.api.ApiConstants.ParameterValues.HOURLY_VALUES
import data.source.api.dto.ForecastResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ApiServiceImpl(private val httpClient: HttpClient) : WeatherApi {
    override fun getWeather(
        latitude: Double,
        longitude: Double
    ): Flow<Result<ForecastResponse>> =
        flow {
            try {
                val response = httpClient.get("https://api.open-meteo.com/v1/forecast") {
                    url {
                        parameters.append(LATITUDE, latitude.toString())
                        parameters.append(LONGITUDE, longitude.toString())
                        parameters.append(CURRENT, CURRENT_VALUES)
                        parameters.append(HOURLY, HOURLY_VALUES)
                        parameters.append(DAILY, DAILY_VALUES)
                        parameters.append(TEMPERATURE_UNIT, DEFAULT_TEMPERATURE_UNIT)
                        parameters.append(WIND_SPEED_UNIT, DEFAULT_WIND_SPEED_UNIT)
                        parameters.append(PRECIPITATION_UNIT, DEFAULT_PRECIPITATION_UNIT)
                        parameters.append(TIMEZONE, DEFAULT_TIMEZONE)
                    }
                }
                    .body<ForecastResponse>()
                emit(Result.success(response))
            } catch (exception: Exception) {
                exception.printStackTrace()
                emit(
                    Result.failure(
                        Error(exception.message, exception)
                    )
                )
            }
        }
}
