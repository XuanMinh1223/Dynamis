package ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dynamis.composeapp.generated.resources.Res
import dynamis.composeapp.generated.resources.home_screen
import org.jetbrains.compose.resources.StringResource
import ui.screen.home.HomeRoute

@Composable
fun DynamisApp(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = DynamisScreen.Home.name,
        modifier = Modifier
            .fillMaxSize()
    ) {
        composable(route = DynamisScreen.Home.name) {
            HomeRoute()
        }
    }
}

enum class DynamisScreen(val title: StringResource) {
    Home(title = Res.string.home_screen),
}
