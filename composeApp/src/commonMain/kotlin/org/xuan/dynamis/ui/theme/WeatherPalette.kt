package org.xuan.dynamis.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/** Weather families represented by the WMO weather codes returned by Open-Meteo. */
enum class WeatherPattern(
    val daylightPalette: WeatherColorPalette,
    /** How much warm horizon light gets through the cloud cover. */
    val sunlight: Float,
) {
    // Sky, horizon, cloud, and shadow tones, followed by sunlight strength.
    ClearSky(skyPalette(0xFF126BB7, 0xFF6ECCFA, 0xFF258EE2, 0xFF164E8B), sunlight = 1f),
    MainlyClear(skyPalette(0xFF237ABB, 0xFF90D4F4, 0xFF57A2D8, 0xFF225D91), sunlight = 0.85f),
    PartlyCloudy(skyPalette(0xFF4C7FAD, 0xFFB2C6D6, 0xFF7998B7, 0xFF314F74), sunlight = 0.65f),
    Overcast(skyPalette(0xFF5A6978, 0xFF9DA8B0, 0xFF788794, 0xFF364553), sunlight = 0.25f),
    Fog(skyPalette(0xFF65777B, 0xFFB7C4C2, 0xFF93A3A3, 0xFF46575C), sunlight = 0.15f),
    Drizzle(skyPalette(0xFF486C78, 0xFF8FB7BA, 0xFF65979B, 0xFF2B4B59), sunlight = 0.25f),
    FreezingDrizzle(skyPalette(0xFF607B90, 0xFFB7D3DC, 0xFF8DAFBD, 0xFF3D556D), sunlight = 0.2f),
    Rain(skyPalette(0xFF344B65, 0xFF779AAC, 0xFF527A91, 0xFF20374F), sunlight = 0.1f),
    FreezingRain(skyPalette(0xFF3E5875, 0xFF92B6CD, 0xFF688FAF, 0xFF293E5B), sunlight = 0.08f),
    Snowfall(skyPalette(0xFF66798F, 0xFFCDD9E0, 0xFFA7B8CB, 0xFF45546C), sunlight = 0.35f),
    RainShowers(skyPalette(0xFF245A7F, 0xFF86BAC8, 0xFF4D91A7, 0xFF203E60), sunlight = 0.5f),
    SnowShowers(skyPalette(0xFF526B8F, 0xFFBECDDF, 0xFF8FA7C7, 0xFF334A6D), sunlight = 0.4f),
    Thunderstorm(skyPalette(0xFF363954, 0xFF7A7697, 0xFF535573, 0xFF1D243B), sunlight = 0.05f),
    Unknown(skyPalette(0xFF546F88, 0xFF9AB6C7, 0xFF7D96A9, 0xFF354D66), sunlight = 0.45f),
    ;

    val displayName: String
        get() = name.replace(Regex("([a-z])([A-Z])"), "\$1 \$2")

    companion object {
        fun fromWeatherCode(code: Int?): WeatherPattern =
            when (code) {
                0 -> ClearSky
                1 -> MainlyClear
                2 -> PartlyCloudy
                3 -> Overcast
                45, 48 -> Fog
                51, 53, 55 -> Drizzle
                56, 57 -> FreezingDrizzle
                61, 63, 65 -> Rain
                66, 67 -> FreezingRain
                71, 73, 75, 77 -> Snowfall
                80, 81, 82 -> RainShowers
                85, 86 -> SnowShowers
                95, 96, 99 -> Thunderstorm
                else -> Unknown
            }
    }
}

enum class TimeOfDay {
    Dawn,
    Day,
    Dusk,
    Night,
    ;

    companion object {
        /** Uses the location's local observation hour. */
        fun fromHour(hour: Int): TimeOfDay =
            when (hour) {
                in 5..7 -> Dawn
                in 8..16 -> Day
                in 17..19 -> Dusk
                else -> Night
            }
    }
}

data class WeatherColorPalette(
    val background: Color,
    val glow: Color,
    val shadow: Color,
    val weatherPrimary: Color,
    val weatherSecondary: Color,
    val foreground: Color,
)

val LocalWeatherColorPalette =
    staticCompositionLocalOf {
        weatherColorPalette(WeatherPattern.Unknown, TimeOfDay.Day)
    }

/** Weather defines the sky; local time tints its atmosphere and horizon. */
fun weatherColorPalette(
    pattern: WeatherPattern,
    timeOfDay: TimeOfDay,
): WeatherColorPalette {
    val daylight = pattern.daylightPalette
    val sunlight = pattern.sunlight
    return when (timeOfDay) {
        TimeOfDay.Day -> daylight
        TimeOfDay.Dawn ->
            daylight.copy(
                background = lerp(daylight.background, Color(0xFF5C5F91), 0.5f),
                glow = lerp(daylight.glow, Color(0xFFFFB270), 0.1f + sunlight * 0.8f),
                shadow = lerp(daylight.shadow, Color(0xFF343B67), 0.5f),
                weatherPrimary = lerp(daylight.weatherPrimary, Color(0xFF8685B1), 0.45f),
                weatherSecondary = lerp(daylight.weatherSecondary, Color(0xFFECA46B), sunlight * 0.9f),
            )
        TimeOfDay.Dusk ->
            daylight.copy(
                background = lerp(daylight.background, Color(0xFF51476E), 0.7f),
                glow = lerp(daylight.glow, Color(0xFFFF814D), 0.08f + sunlight * 0.85f),
                shadow = lerp(daylight.shadow, Color(0xFF2C2C52), 0.65f),
                weatherPrimary = lerp(daylight.weatherPrimary, Color(0xFF7C6593), 0.6f),
                weatherSecondary = lerp(daylight.weatherSecondary, Color(0xFFED8C8C), 0.1f + sunlight * 0.85f),
            )
        TimeOfDay.Night -> {
            // Retain the weather's hue as the sky darkens to midnight.
            val midnight = lerp(daylight.shadow, Color(0xFF020818), 0.8f)
            daylight.copy(
                background = lerp(daylight.background, midnight, 0.78f),
                glow = lerp(daylight.glow, midnight, 0.68f),
                shadow = midnight,
                weatherPrimary = lerp(daylight.weatherPrimary, midnight, 0.7f),
                weatherSecondary = lerp(daylight.weatherSecondary, midnight, 0.7f),
            )
        }
    }
}

private fun skyPalette(
    sky: Long,
    horizon: Long,
    cloud: Long,
    shade: Long,
) = WeatherColorPalette(
    background = Color(sky),
    glow = Color(horizon),
    shadow = Color(shade),
    weatherPrimary = Color(cloud),
    weatherSecondary = lerp(Color(cloud), Color(horizon), 0.45f),
    foreground = Color(0xFFF7FAFF),
)
