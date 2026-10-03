package org.xuan.dynamis.ui.screen.radar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.RasterLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.TileSetOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.sources.rememberRasterSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import org.xuan.dynamis.data.repo.radarCoverageTileUrl
import org.xuan.dynamis.data.repo.radarTileUrl
import org.xuan.dynamis.data.source.api.ApiConstants
import org.xuan.dynamis.domain.model.GeoCoordinates
import org.xuan.dynamis.domain.model.RadarFrame
import org.xuan.dynamis.domain.model.RadarTileOptions
import kotlin.time.Duration.Companion.milliseconds

private val BaseMapStyle = BaseStyle.Uri(ApiConstants.BASE_MAP_STYLE_URL)
private const val RADAR_OPACITY = 0.75f

/** Centred on [center] at regional zoom, or a continental view when the location is unknown. */
internal fun radarCameraPosition(center: GeoCoordinates?): CameraPosition =
    if (center != null) {
        CameraPosition(target = Position(longitude = center.longitude, latitude = center.latitude), zoom = 6.0)
    } else {
        CameraPosition(target = Position(longitude = -98.0, latitude = 39.0), zoom = 3.0)
    }

/**
 * The shared radar map. With [interactive] false the map ignores touches and only shows the
 * selected frame; otherwise every frame is mounted so stepping through them doesn't flicker.
 */
@Composable
internal fun RadarMap(
    state: RadarUiState.Ready,
    cameraState: CameraState,
    interactive: Boolean,
    locationColor: Color,
    modifier: Modifier = Modifier,
) {
    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseMapStyle,
        cameraState = cameraState,
        // The radar tiles stop at zoom 7; MapLibre upscales beyond that, so keep the view useful.
        zoomRange = 2f..10f,
        options = if (interactive) {
            MapOptions(gestureOptions = GestureOptions.RotationLocked)
        } else {
            MapOptions(gestureOptions = GestureOptions.AllDisabled, ornamentOptions = OrnamentOptions.AllDisabled)
        },
    ) {
        if (state.showCoverage) CoverageLayer(state.frames.first().host)
        // Hidden layers still load their tiles, so the animation preloads every frame.
        state.frames.forEachIndexed { index, frame ->
            if (interactive || index == state.selectedIndex) {
                RadarFrameLayer(frame, state.tileOptions, visible = index == state.selectedIndex)
            }
        }
        state.center?.let { LocationDot(it, locationColor) }
    }
}

@Composable
private fun CoverageLayer(host: String) {
    val source = rememberRasterSource(
        tiles = listOf(radarCoverageTileUrl(host)),
        options = TileSetOptions(maxZoom = ApiConstants.RadarTiles.MAX_ZOOM),
        tileSize = ApiConstants.RadarTiles.SIZE,
    )
    RasterLayer(id = "radar-coverage", source = source, opacity = const(0.4f))
}

@Composable
private fun RadarFrameLayer(frame: RadarFrame, options: RadarTileOptions, visible: Boolean) {
    val source = rememberRasterSource(
        tiles = listOf(radarTileUrl(frame, options)),
        options = TileSetOptions(maxZoom = ApiConstants.RadarTiles.MAX_ZOOM),
        tileSize = ApiConstants.RadarTiles.SIZE,
    )
    RasterLayer(
        // The options are part of the id because changing them swaps the layer's source.
        id = "radar-${frame.epochSeconds}-${options.smooth}-${options.snow}",
        source = source,
        opacity = const(if (visible) RADAR_OPACITY else 0f),
        fadeDuration = const(0.milliseconds),
    )
}

@Composable
private fun LocationDot(location: GeoCoordinates, color: Color) {
    val source = rememberGeoJsonSource(
        GeoJsonData.JsonString("""{"type":"Point","coordinates":[${location.longitude},${location.latitude}]}"""),
    )
    CircleLayer(id = "location-halo", source = source, color = const(color), opacity = const(0.25f), radius = const(16.dp))
    CircleLayer(
        id = "location-dot",
        source = source,
        color = const(color),
        radius = const(7.dp),
        strokeColor = const(Color.White),
        strokeWidth = const(2.5.dp),
    )
}
