package org.xuan.dynamis.data.source.api

import org.xuan.dynamis.data.source.api.ApiConstants.ParameterNames.CURRENT
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterNames.DAILY
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterNames.HOURLY
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterNames.LATITUDE
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterNames.LONGITUDE
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterNames.TEMPERATURE_UNIT
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterNames.TIMEZONE
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterValues.CURRENT_VALUES
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterValues.DAILY_VALUES
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterValues.DEFAULT_TEMPERATURE_UNIT
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterValues.DEFAULT_TIMEZONE
import org.xuan.dynamis.data.source.api.ApiConstants.ParameterValues.HOURLY_VALUES
import org.xuan.dynamis.data.source.api.dto.ForecastResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class KtorWeatherApi(private val httpClient: HttpClient) : WeatherApi {
    override suspend fun getWeather(latitude: Double, longitude: Double): ForecastResponse =
        httpClient.get(ApiConstants.FORECAST_URL) {
            url {
                parameters.append(LATITUDE, latitude.toString())
                parameters.append(LONGITUDE, longitude.toString())
                parameters.append(CURRENT, CURRENT_VALUES)
                parameters.append(HOURLY, HOURLY_VALUES)
                parameters.append(DAILY, DAILY_VALUES)
                parameters.append(TEMPERATURE_UNIT, DEFAULT_TEMPERATURE_UNIT)
                parameters.append(TIMEZONE, DEFAULT_TIMEZONE)
            }
        }.body()
}
