package org.xuan.dynamis.data.source.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RadarResponse(
    val host: String,
    val radar: RadarFramesDto,
)

@Serializable
data class RadarFramesDto(
    val past: List<RadarFrameDto> = emptyList(),
    val nowcast: List<RadarFrameDto> = emptyList(),
)

@Serializable
data class RadarFrameDto(
    val time: Long,
    val path: String,
)
