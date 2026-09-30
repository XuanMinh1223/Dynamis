package org.xuan.dynamis.data.source.api

object ApiConstants {
    const val FORECAST_URL = "https://api.open-meteo.com/v1/forecast"
    object ParameterNames {
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val CURRENT = "current"
        const val HOURLY = "hourly"
        const val DAILY = "daily"
        const val TEMPERATURE_UNIT = "temperature_unit"
        const val TIMEZONE = "timezone"
    }
    object ParameterValues {
        const val CURRENT_VALUES =
            "temperature_2m,relative_humidity_2m,apparent_temperature,is_day," +
                "precipitation,rain,showers,snowfall,weather_code,cloud_cover,surface_pressure," +
                "wind_speed_10m,wind_direction_10m,wind_gusts_10m"
        const val HOURLY_VALUES =
            "temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature," +
                "precipitation_probability,precipitation,rain,showers,snowfall,snow_depth,weather_code," +
                "surface_pressure,cloud_cover,cloud_cover_low,cloud_cover_mid,cloud_cover_high,visibility," +
                "wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index,uv_index_clear_sky,is_day," +
                "sunshine_duration"
        const val DAILY_VALUES =
            "temperature_2m_max,temperature_2m_min,precipitation_sum,rain_sum,showers_sum,snowfall_sum," +
                "precipitation_hours,precipitation_probability_max,wind_speed_10m_max,wind_gusts_10m_max," +
                "wind_direction_10m_dominant,uv_index_max,sunrise,sunset"
        const val DEFAULT_TIMEZONE = "auto"
        const val DEFAULT_TEMPERATURE_UNIT = "celsius"
    }
}
