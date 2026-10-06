package org.xuan.dynamis.data.repo

import org.xuan.dynamis.data.source.api.ApiConstants.RadarTiles
import org.xuan.dynamis.domain.model.RadarFrame
import org.xuan.dynamis.domain.model.RadarTileOptions

/** Tile URL template for [frame], with `{z}`, `{x}` and `{y}` placeholders. */
fun radarTileUrl(
    frame: RadarFrame,
    options: RadarTileOptions = RadarTileOptions(),
): String =
    "${frame.host.trimEnd('/')}${frame.path}/${RadarTiles.SIZE}/{z}/{x}/{y}/${RadarTiles.COLOR_SCHEME}/" +
        "${options.smooth.bit()}_${options.snow.bit()}.png"

/** Tiles marking where radar data exists. */
fun radarCoverageTileUrl(host: String): String = "${host.trimEnd('/')}/v2/coverage/0/${RadarTiles.SIZE}/{z}/{x}/{y}/0/0_0.png"

private fun Boolean.bit() = if (this) 1 else 0
