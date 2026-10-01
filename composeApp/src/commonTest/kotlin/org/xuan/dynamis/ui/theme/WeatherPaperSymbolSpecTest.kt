package org.xuan.dynamis.ui.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class WeatherPaperSymbolSpecTest {
    @Test
    fun everySupportedWeatherCodeHasItsOwnPaperComposition() {
        val codes = listOf(
            0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65,
            66, 67, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99,
        )
        val specs = codes.map(::paperWeatherSpec)

        assertTrue(codes.all { WeatherPattern.fromWeatherCode(it) != WeatherPattern.Unknown })
        assertTrue(specs.all { it.kind != PaperWeatherKind.Unknown })
        assertEquals(codes.size, specs.toSet().size)
    }

    @Test
    fun rimeFreezingPrecipitationAndHailHaveTheirOwnDetails() {
        assertTrue(paperWeatherSpec(48).rime)
        assertTrue(paperWeatherSpec(56).icy)
        assertTrue(paperWeatherSpec(66).icy)
        assertTrue(paperWeatherSpec(96).hail)
        assertNotEquals(paperWeatherSpec(95), paperWeatherSpec(99))
    }
}
