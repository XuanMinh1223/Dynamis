package org.xuan.dynamis.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.xuan.dynamis.ui.screen.home.HomeRoute
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
            HomeRoute()
        }
    }
}

@Serializable
data object HomeDestination
