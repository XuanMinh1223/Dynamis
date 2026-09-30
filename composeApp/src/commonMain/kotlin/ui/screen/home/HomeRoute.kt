package ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeRoute(viewModel: HomeViewModel = koinViewModel()) {
    val isShowing by viewModel.isShowing.collectAsStateWithLifecycle()
    HomeScreen(weather = viewModel.weatherUIState.value, isShowing = isShowing)
}
