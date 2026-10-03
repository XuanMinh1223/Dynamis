package org.xuan.dynamis.ui.screen.radar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.rememberCameraState
import org.xuan.dynamis.resources.Res
import org.xuan.dynamis.resources.open_radar
import org.xuan.dynamis.resources.radar
import org.xuan.dynamis.resources.radar_attribution
import org.xuan.dynamis.resources.radar_latest_scan
import org.xuan.dynamis.resources.radar_loading
import org.xuan.dynamis.resources.radar_tap_to_open
import org.xuan.dynamis.resources.radar_unavailable
import org.xuan.dynamis.resources.retry
import org.xuan.dynamis.ui.theme.LocalWeatherColorPalette

/** A static, non-interactive radar snapshot centred on the user. Tapping it opens the full radar. */
@Composable
fun RadarPreviewCard(
    state: RadarUiState,
    onRetry: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(240.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        when (state) {
            RadarUiState.Loading -> Message {
                CircularProgressIndicator()
                Text(stringResource(Res.string.radar_loading))
            }

            RadarUiState.Error -> Message {
                Text(stringResource(Res.string.radar_unavailable), textAlign = TextAlign.Center)
                Button(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
            }

            is RadarUiState.Ready -> ReadyPreview(state, onOpen)
        }
    }
}

@Composable
private fun ReadyPreview(state: RadarUiState.Ready, onOpen: () -> Unit) {
    val cameraState = rememberCameraState(firstPosition = radarCameraPosition(state.center))
    val description = stringResource(Res.string.open_radar)
    Box(Modifier.fillMaxSize()) {
        RadarMap(
            state = state,
            cameraState = cameraState,
            interactive = false,
            locationColor = LocalWeatherColorPalette.current.background,
            modifier = Modifier.fillMaxSize(),
        )
        // Sits above the map so taps open the full screen instead of reaching the native map view.
        Box(
            Modifier.fillMaxSize()
                .semantics { contentDescription = description }
                .clickable(role = Role.Button, onClick = onOpen),
        )
        Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ) {
            Text(
                text = "${stringResource(Res.string.radar)} · " +
                    stringResource(Res.string.radar_latest_scan, formatRadarTime(state.selectedFrame.epochSeconds)),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ) {
            Text(
                text = stringResource(Res.string.radar_tap_to_open),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        Text(
            text = stringResource(Res.string.radar_attribution),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
        )
    }
}

@Composable
private fun Message(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}
