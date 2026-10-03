package org.xuan.dynamis.data.repo

import org.xuan.dynamis.data.source.api.RadarApi
import org.xuan.dynamis.domain.RadarRepository
import org.xuan.dynamis.domain.model.RadarFrame

class DefaultRadarRepository(private val api: RadarApi) : RadarRepository {
    override suspend fun getRadarFrames(): List<RadarFrame> = api.getRadarFrames().toRadarFrames()
}
