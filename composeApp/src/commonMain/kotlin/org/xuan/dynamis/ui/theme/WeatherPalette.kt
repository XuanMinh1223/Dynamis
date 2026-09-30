package org.xuan.dynamis.ui.theme

import androidx.compose.ui.graphics.Color

/** Weather families represented by the WMO weather codes returned by Open-Meteo. */
enum class WeatherPattern(
    val primaryColor: Color,
    val secondaryColor: Color,
) {
    ClearSky(Color(0xFFFFC96B), Color(0xFF77C9D4)),
    MainlyClear(Color(0xFFFFD18A), Color(0xFF75BDD2)),
    PartlyCloudy(Color(0xFF91BCE8), Color(0xFFB3A6DE)),
    Overcast(Color(0xFF8298AE), Color(0xFF9EB5BD)),
    Fog(Color(0xFFB7C8CC), Color(0xFF9AB9C7)),
    Drizzle(Color(0xFF69B9B6), Color(0xFF83AFCF)),
    FreezingDrizzle(Color(0xFF9DDBDE), Color(0xFFB4B3E2)),
    Rain(Color(0xFF568FC4), Color(0xFF59B7BC)),
    FreezingRain(Color(0xFF8CCBE2), Color(0xFF9C9BD2)),
    Snowfall(Color(0xFFA9D7EB), Color(0xFFB9B4E1)),
    RainShowers(Color(0xFF4D9DD0), Color(0xFF65C5BE)),
    SnowShowers(Color(0xFFB8E4EE), Color(0xFFA5B8E1)),
    Thunderstorm(Color(0xFF806BB8), Color(0xFF557EB8)),
    Unknown(Color(0xFF91B6C4), Color(0xFF9D9FCB));

    companion object {
        fun fromWeatherCode(code: Int?): WeatherPattern = when (code) {
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

enum class TimeOfDay(
    val backgroundColor: Color,
    val glowColor: Color,
    val shadowColor: Color,
    val foregroundColor: Color,
) {
    Dawn(
        backgroundColor = Color(0xFFEBC2B2),
        glowColor = Color(0xFFFFE3B7),
        shadowColor = Color(0xFFD99BB4),
        foregroundColor = Color(0xFF342A38),
    ),
    Day(
        backgroundColor = Color(0xFFA9D0DC),
        glowColor = Color(0xFFE3F0D8),
        shadowColor = Color(0xFF80B5C9),
        foregroundColor = Color(0xFF193440),
    ),
    Dusk(
        backgroundColor = Color(0xFFB58EBA),
        glowColor = Color(0xFFF1B69A),
        shadowColor = Color(0xFF766B9E),
        foregroundColor = Color(0xFF30233F),
    ),
    Night(
        backgroundColor = Color(0xFF172A49),
        glowColor = Color(0xFF526F99),
        shadowColor = Color(0xFF101A35),
        foregroundColor = Color(0xFFF0F3FC),
    );

    companion object {
        /** Uses the location's local observation hour. */
        fun fromHour(hour: Int): TimeOfDay = when (hour) {
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

fun weatherColorPalette(pattern: WeatherPattern, timeOfDay: TimeOfDay) = WeatherColorPalette(
    background = timeOfDay.backgroundColor,
    glow = timeOfDay.glowColor,
    shadow = timeOfDay.shadowColor,
    weatherPrimary = pattern.primaryColor,
    weatherSecondary = pattern.secondaryColor,
    foreground = timeOfDay.foregroundColor,
)
