package org.xuan.dynamis.data.source.api

import org.xuan.dynamis.data.source.api.dto.RadarResponse

interface RadarApi {
    suspend fun getRadarFrames(): RadarResponse
}
