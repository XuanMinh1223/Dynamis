package org.xuan.dynamis.domain.model

/** One radar snapshot, addressed by the tile server [host] and the snapshot's [path]. */
data class RadarFrame(
    val epochSeconds: Long,
    val host: String,
    val path: String,
    val isForecast: Boolean,
)

/** Rendering choices RainViewer applies to its tiles. */
data class RadarTileOptions(
    /** Has RainViewer blur the radar data, which softens the edges of its cells. */
    val smooth: Boolean = true,
    /** Colors snow differently from rain. */
    val snow: Boolean = true,
)
