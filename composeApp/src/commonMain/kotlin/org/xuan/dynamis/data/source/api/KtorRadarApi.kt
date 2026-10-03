package org.xuan.dynamis.data.source.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.xuan.dynamis.data.source.api.dto.RadarResponse

class KtorRadarApi(private val httpClient: HttpClient) : RadarApi {
    override suspend fun getRadarFrames(): RadarResponse = httpClient.get(ApiConstants.RADAR_MAPS_URL).body()
}
