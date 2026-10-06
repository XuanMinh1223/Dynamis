package org.xuan.dynamis.data.repo

import org.xuan.dynamis.data.source.api.dto.RadarFrameDto
import org.xuan.dynamis.data.source.api.dto.RadarResponse
import org.xuan.dynamis.domain.model.RadarFrame

fun RadarResponse.toRadarFrames(): List<RadarFrame> =
    (radar.past.map { it.toFrame(host, isForecast = false) } + radar.nowcast.map { it.toFrame(host, isForecast = true) })
        .sortedBy { it.epochSeconds }

private fun RadarFrameDto.toFrame(
    host: String,
    isForecast: Boolean,
) = RadarFrame(epochSeconds = time, host = host, path = path, isForecast = isForecast)
