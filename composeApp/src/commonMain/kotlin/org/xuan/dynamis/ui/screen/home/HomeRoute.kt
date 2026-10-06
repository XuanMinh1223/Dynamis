package org.xuan.dynamis.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.xuan.dynamis.ui.screen.radar.RadarSection
import org.xuan.dynamis.ui.theme.TimeOfDay
import org.xuan.dynamis.ui.theme.WeatherPattern

@Composable
fun HomeRoute(
    onOpenRadar: (WeatherPattern, TimeOfDay) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onRetry = viewModel::refresh,
        radarContent = {
            val weather = (state as? HomeUiState.Success)?.weather
            RadarSection(
                onOpen = {
                    onOpenRadar(weather?.weatherPattern ?: WeatherPattern.Unknown, weather?.timeOfDay ?: TimeOfDay.Day)
                },
            )
        },
    )
}
