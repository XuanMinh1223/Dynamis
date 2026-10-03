package org.xuan.dynamis.ui.screen.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.xuan.dynamis.domain.LocationProvider
import org.xuan.dynamis.domain.RadarRepository
import org.xuan.dynamis.domain.model.RadarFrame

class RadarViewModel(
    private val repository: RadarRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<RadarUiState>(RadarUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var playJob: Job? = null

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        playJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = RadarUiState.Loading
            try {
                val frames = repository.getRadarFrames()
                if (frames.isEmpty()) {
                    _uiState.value = RadarUiState.Error
                    return@launch
                }
                // The map is still useful without a fix, so a location failure only loses the centring.
                val center = runCatching { locationProvider.currentLocation() }
                    .onFailure { if (it is CancellationException) throw it }
                    .getOrNull()
                _uiState.value = RadarUiState.Ready(
                    frames = frames,
                    selectedIndex = frames.lastPastIndex(),
                    isPlaying = false,
                    center = center,
                )
            } catch (cause: CancellationException) {
                throw cause
            } catch (cause: Exception) {
                _uiState.value = RadarUiState.Error
            }
        }
    }

    fun togglePlayback() {
        val state = _uiState.value as? RadarUiState.Ready ?: return
        if (state.isPlaying) {
            pause()
            return
        }
        // Restart from the beginning when resuming on the last frame.
        _uiState.value = state.copy(
            isPlaying = true,
            selectedIndex = if (state.selectedIndex == state.frames.lastIndex) 0 else state.selectedIndex,
        )
        playJob?.cancel()
        playJob = viewModelScope.launch {
            while (isActive) {
                delay(FRAME_DURATION_MILLIS)
                _uiState.update { current ->
                    if (current !is RadarUiState.Ready) return@update current
                    val next = (current.selectedIndex + 1) % current.frames.size
                    current.copy(selectedIndex = next)
                }
            }
        }
    }

    fun selectFrame(index: Int) {
        pause()
        _uiState.update { current ->
            if (current is RadarUiState.Ready) {
                current.copy(selectedIndex = index.coerceIn(0, current.frames.lastIndex))
            } else {
                current
            }
        }
    }

    fun setSmooth(smooth: Boolean) = updateReady { it.copy(tileOptions = it.tileOptions.copy(smooth = smooth)) }

    fun setSnow(snow: Boolean) = updateReady { it.copy(tileOptions = it.tileOptions.copy(snow = snow)) }

    fun setShowCoverage(show: Boolean) = updateReady { it.copy(showCoverage = show) }

    private fun updateReady(transform: (RadarUiState.Ready) -> RadarUiState.Ready) {
        _uiState.update { current -> if (current is RadarUiState.Ready) transform(current) else current }
    }

    private fun pause() {
        playJob?.cancel()
        _uiState.update { current -> if (current is RadarUiState.Ready) current.copy(isPlaying = false) else current }
    }

    private fun List<RadarFrame>.lastPastIndex(): Int =
        indexOfLast { !it.isForecast }.coerceAtLeast(0)

    private companion object {
        const val FRAME_DURATION_MILLIS = 600L
    }
}
