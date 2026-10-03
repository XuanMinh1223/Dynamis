package org.xuan.dynamis.data.repo

import org.xuan.dynamis.data.source.api.dto.RadarFrameDto
import org.xuan.dynamis.data.source.api.dto.RadarFramesDto
import org.xuan.dynamis.data.source.api.dto.RadarResponse
import org.xuan.dynamis.domain.model.RadarTileOptions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RadarMapperTest {
    private val response = RadarResponse(
        host = "https://tilecache.rainviewer.com/",
        radar = RadarFramesDto(
            past = listOf(RadarFrameDto(100, "/v2/radar/a"), RadarFrameDto(50, "/v2/radar/b")),
            nowcast = listOf(RadarFrameDto(200, "/v2/radar/c")),
        ),
    )

    @Test
    fun buildsTileTemplateFromHostAndPath() {
        val frame = response.toRadarFrames().first()
        assertEquals("https://tilecache.rainviewer.com/v2/radar/b/256/{z}/{x}/{y}/2/1_1.png", radarTileUrl(frame))
    }

    @Test
    fun tileOptionsControlSmoothAndSnowFlags() {
        val frame = response.toRadarFrames().first()
        assertEquals(
            "https://tilecache.rainviewer.com/v2/radar/b/256/{z}/{x}/{y}/2/0_1.png",
            radarTileUrl(frame, RadarTileOptions(smooth = false)),
        )
        assertEquals(
            "https://tilecache.rainviewer.com/v2/radar/b/256/{z}/{x}/{y}/2/1_0.png",
            radarTileUrl(frame, RadarTileOptions(snow = false)),
        )
    }

    @Test
    fun buildsCoverageTileUrl() {
        assertEquals(
            "https://tilecache.rainviewer.com/v2/coverage/0/256/{z}/{x}/{y}/0/0_0.png",
            radarCoverageTileUrl(response.host),
        )
    }

    @Test
    fun ordersFramesByTimeAndFlagsForecast() {
        val frames = response.toRadarFrames()
        assertEquals(listOf(50L, 100L, 200L), frames.map { it.epochSeconds })
        assertFalse(frames[1].isForecast)
        assertTrue(frames[2].isForecast)
    }
}
