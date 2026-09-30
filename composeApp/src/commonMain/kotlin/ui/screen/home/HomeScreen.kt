package ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import dynamis.composeapp.generated.resources.Res
import dynamis.composeapp.generated.resources.current_location
import dynamis.composeapp.generated.resources.loading_weather
import dynamis.composeapp.generated.resources.location_permission_denied
import dynamis.composeapp.generated.resources.location_permission_denied_forever
import dynamis.composeapp.generated.resources.location_unavailable
import dynamis.composeapp.generated.resources.retry
import dynamis.composeapp.generated.resources.weather_unavailable
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val weather = (state as? HomeUiState.Success)?.weather ?: WeatherUiState()
    val isShowing = state is HomeUiState.Success
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        weather.backgroundGradient.first,
                        weather.backgroundGradient.second
                    )
                )
            )
    ) {
        val transitionState = remember { MutableTransitionState(isShowing) }
        transitionState.targetState = isShowing
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    Icons.Rounded.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterVertically)
                        .padding(8.dp)
                )
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterVertically)
                        .padding(8.dp)
                )
            }
            when (state) {
                HomeUiState.Loading -> {
                    CircularProgressIndicator(Modifier.padding(16.dp))
                    Text(stringResource(Res.string.loading_weather))
                }
                is HomeUiState.Error -> {
                    val message = when (state.reason) {
                        HomeError.PermissionDenied -> Res.string.location_permission_denied
                        HomeError.PermissionDeniedForever -> Res.string.location_permission_denied_forever
                        HomeError.LocationUnavailable -> Res.string.location_unavailable
                        HomeError.WeatherUnavailable -> Res.string.weather_unavailable
                    }
                    Text(stringResource(message), modifier = Modifier.padding(16.dp))
                    Button(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
                }
                is HomeUiState.Success -> Unit
            }
            AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 500),
                    initialOffsetY = { fullHeight -> fullHeight }) + fadeIn(
                    animationSpec = tween(durationMillis = 500)
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = weather.locality.ifBlank { stringResource(Res.string.current_location) },
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = weather.time,
                        style = MaterialTheme.typography.caption

                    )
                    Text(
                        text = weather.currentTemperature,
                        style = MaterialTheme.typography.h1
                    )
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                "high temperature"
                            )
                            Text(weather.todayHigh)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                "low temperature"
                            )
                            Text(weather.todayLow)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            state = HomeUiState.Success(WeatherUiState(locality = "San Francisco, California", currentTemperature = "18°C")),
            onRetry = {},
        )
    }
}
