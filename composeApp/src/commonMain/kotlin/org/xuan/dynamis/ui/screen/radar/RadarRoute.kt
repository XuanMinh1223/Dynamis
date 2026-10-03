package org.xuan.dynamis.ui.screen.radar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.xuan.dynamis.ui.theme.LocalWeatherColorPalette
import org.xuan.dynamis.ui.theme.TimeOfDay
import org.xuan.dynamis.ui.theme.WeatherPattern
import org.xuan.dynamis.ui.theme.weatherColorPalette

/** The home-screen radar preview; [onOpen] navigates to the full screen. */
@Composable
fun RadarSection(onOpen: () -> Unit, viewModel: RadarViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RadarPreviewCard(state = state, onRetry = viewModel::load, onOpen = onOpen)
}

/** The full radar screen, themed to match the home screen's current weather. */
@Composable
fun RadarRoute(
    pattern: WeatherPattern,
    timeOfDay: TimeOfDay,
    onBack: () -> Unit,
    viewModel: RadarViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalWeatherColorPalette provides weatherColorPalette(pattern, timeOfDay)) {
        RadarScreen(
            state = state,
            actions = RadarActions(
                onBack = onBack,
                onRetry = viewModel::load,
                onTogglePlayback = viewModel::togglePlayback,
                onSelectFrame = viewModel::selectFrame,
                onSmoothChange = viewModel::setSmooth,
                onSnowChange = viewModel::setSnow,
                onCoverageChange = viewModel::setShowCoverage,
            ),
        )
    }
}
