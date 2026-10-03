package org.xuan.dynamis.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.xuan.dynamis.ui.screen.home.HomeRoute
import org.xuan.dynamis.ui.screen.radar.RadarRoute
import org.xuan.dynamis.ui.theme.TimeOfDay
import org.xuan.dynamis.ui.theme.WeatherPattern
import kotlinx.serialization.Serializable

@Composable
fun DynamisApp(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = Modifier
            .fillMaxSize()
    ) {
        composable<HomeDestination> {
            HomeRoute(
                onOpenRadar = { pattern, timeOfDay -> navController.navigate(RadarDestination(pattern, timeOfDay)) },
            )
        }
        composable<RadarDestination> { entry ->
            val destination = entry.toRoute<RadarDestination>()
            RadarRoute(
                pattern = destination.pattern,
                timeOfDay = destination.timeOfDay,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Serializable
data object HomeDestination

@Serializable
data class RadarDestination(val pattern: WeatherPattern, val timeOfDay: TimeOfDay)
