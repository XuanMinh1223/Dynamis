package org.xuan.dynamis.ui.screen.radar

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.rememberCameraState
import org.xuan.dynamis.resources.Res
import org.xuan.dynamis.resources.radar_attribution
import org.xuan.dynamis.resources.radar_back
import org.xuan.dynamis.resources.radar_coverage
import org.xuan.dynamis.resources.radar_forecast_frame
import org.xuan.dynamis.resources.radar_latest_scan
import org.xuan.dynamis.resources.radar_loading
import org.xuan.dynamis.resources.radar_pause
import org.xuan.dynamis.resources.radar_play
import org.xuan.dynamis.resources.radar_recenter
import org.xuan.dynamis.resources.radar_smooth
import org.xuan.dynamis.resources.radar_snow_colors
import org.xuan.dynamis.resources.radar_timeline
import org.xuan.dynamis.resources.radar_unavailable
import org.xuan.dynamis.resources.retry
import org.xuan.dynamis.ui.theme.LocalWeatherColorPalette
import org.xuan.dynamis.ui.theme.WeatherColorPalette

// icons-core ships no pause glyph, so draw the standard two-bar one.
private val PauseIcon: ImageVector =
    ImageVector.Builder("Pause", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 19f)
            horizontalLineToRelative(4f)
            verticalLineTo(5f)
            horizontalLineTo(6f)
            verticalLineToRelative(14f)
            close()
            moveTo(14f, 5f)
            verticalLineToRelative(14f)
            horizontalLineToRelative(4f)
            verticalLineTo(5f)
            horizontalLineToRelative(-4f)
            close()
        }
    }.build()

class RadarActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onTogglePlayback: () -> Unit,
    val onSelectFrame: (Int) -> Unit,
    val onSmoothChange: (Boolean) -> Unit,
    val onSnowChange: (Boolean) -> Unit,
    val onCoverageChange: (Boolean) -> Unit,
)

/** The dedicated, fully interactive radar screen. */
@Composable
fun RadarScreen(
    state: RadarUiState,
    actions: RadarActions,
    modifier: Modifier = Modifier,
) {
    val palette = LocalWeatherColorPalette.current
    Surface(modifier = modifier.fillMaxSize(), color = palette.background, contentColor = palette.foreground) {
        when (state) {
            RadarUiState.Loading ->
                CenteredMessage {
                    CircularProgressIndicator(color = palette.foreground)
                    Text(stringResource(Res.string.radar_loading))
                }

            RadarUiState.Error ->
                CenteredMessage {
                    Text(stringResource(Res.string.radar_unavailable), textAlign = TextAlign.Center)
                    Button(onClick = actions.onRetry) { Text(stringResource(Res.string.retry)) }
                }

            is RadarUiState.Ready -> ReadyRadar(state, actions, palette)
        }
        // Over a loading or error message the map is absent, but the way out must remain.
        if (state !is RadarUiState.Ready) {
            Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing).padding(8.dp)) {
                BackButton(actions.onBack, palette)
            }
        }
    }
}

@Composable
private fun ReadyRadar(
    state: RadarUiState.Ready,
    actions: RadarActions,
    palette: WeatherColorPalette,
) {
    val cameraState = rememberCameraState(firstPosition = radarCameraPosition(state.center))
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        RadarMap(
            state = state,
            cameraState = cameraState,
            interactive = true,
            locationColor = palette.background,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(8.dp)) {
            BackButton(actions.onBack, palette, Modifier.align(Alignment.TopStart))
            if (state.center != null) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(50),
                    color = palette.shadow.copy(alpha = 0.9f),
                    contentColor = palette.foreground,
                ) {
                    IconButton(
                        onClick = { scope.launch { cameraState.animateTo(radarCameraPosition(state.center)) } },
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = stringResource(Res.string.radar_recenter))
                    }
                }
            }
            RadarControls(state, actions, palette, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun CenteredMessage(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
private fun BackButton(
    onBack: () -> Unit,
    palette: WeatherColorPalette,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = palette.shadow.copy(alpha = 0.9f),
        contentColor = palette.foreground,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(Res.string.radar_back))
        }
    }
}

@Composable
private fun RadarControls(
    state: RadarUiState.Ready,
    actions: RadarActions,
    palette: WeatherColorPalette,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = palette.shadow.copy(alpha = 0.92f),
        contentColor = palette.foreground,
    ) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = actions.onTogglePlayback) {
                    Icon(
                        imageVector = if (state.isPlaying) PauseIcon else Icons.Rounded.PlayArrow,
                        contentDescription =
                            stringResource(
                                if (state.isPlaying) Res.string.radar_pause else Res.string.radar_play,
                            ),
                    )
                }
                val timelineDescription = stringResource(Res.string.radar_timeline)
                Slider(
                    value = state.selectedIndex.toFloat(),
                    onValueChange = { actions.onSelectFrame(it.toInt()) },
                    valueRange = 0f..state.frames.lastIndex.toFloat().coerceAtLeast(1f),
                    steps = (state.frames.size - 2).coerceAtLeast(0),
                    colors =
                        SliderDefaults.colors(
                            thumbColor = palette.foreground,
                            activeTrackColor = palette.foreground,
                            inactiveTrackColor = palette.foreground.copy(alpha = 0.3f),
                            activeTickColor = palette.shadow,
                            inactiveTickColor = palette.foreground.copy(alpha = 0.5f),
                        ),
                    modifier = Modifier.weight(1f).semantics { contentDescription = timelineDescription },
                )
                val label = formatRadarTime(state.selectedFrame.epochSeconds)
                Text(
                    text =
                        if (state.selectedFrame.isForecast) {
                            stringResource(Res.string.radar_forecast_frame, label)
                        } else {
                            label
                        },
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OptionChip(stringResource(Res.string.radar_smooth), state.tileOptions.smooth, palette, actions.onSmoothChange)
                OptionChip(stringResource(Res.string.radar_snow_colors), state.tileOptions.snow, palette, actions.onSnowChange)
                OptionChip(stringResource(Res.string.radar_coverage), state.showCoverage, palette, actions.onCoverageChange)
            }
            Text(
                text =
                    stringResource(
                        Res.string.radar_latest_scan,
                        formatRadarTime((state.frames.lastOrNull { !it.isForecast } ?: state.frames.last()).epochSeconds),
                    ) + " · " + stringResource(Res.string.radar_attribution),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun OptionChip(
    label: String,
    selected: Boolean,
    palette: WeatherColorPalette,
    onChange: (Boolean) -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = { onChange(!selected) },
        label = { Text(label) },
        colors =
            FilterChipDefaults.filterChipColors(
                labelColor = palette.foreground,
                selectedLabelColor = palette.foreground,
                selectedContainerColor = palette.foreground.copy(alpha = 0.28f),
            ),
    )
}
