package org.xuan.dynamis.domain

import org.xuan.dynamis.domain.model.RadarFrame

interface RadarRepository {
    /** Radar frames ordered oldest to newest. */
    suspend fun getRadarFrames(): List<RadarFrame>
}
