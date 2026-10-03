package org.xuan.dynamis.ui.screen.radar

import org.xuan.dynamis.domain.model.GeoCoordinates
import org.xuan.dynamis.domain.model.RadarFrame
import org.xuan.dynamis.domain.model.RadarTileOptions

sealed interface RadarUiState {
    data object Loading : RadarUiState
    data object Error : RadarUiState
    data class Ready(
        val frames: List<RadarFrame>,
        val selectedIndex: Int,
        val isPlaying: Boolean,
        /** The user's location; null when the device location is unavailable. */
        val center: GeoCoordinates?,
        val tileOptions: RadarTileOptions = RadarTileOptions(),
        val showCoverage: Boolean = false,
    ) : RadarUiState {
        val selectedFrame: RadarFrame get() = frames[selectedIndex]
    }
}
