package com.example.tobaccocellar.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import com.example.tobaccocellar.ui.home.HomeDestination
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tobaccocellar.ui.home.HomeScreen
import com.example.tobaccocellar.ui.items.AddEntryDestination
import com.example.tobaccocellar.ui.items.AddEntryScreen
import com.example.tobaccocellar.ui.items.EditEntryDestination
import com.example.tobaccocellar.ui.items.EditEntryScreen
import com.example.tobaccocellar.ui.settings.SettingsDestination
import com.example.tobaccocellar.ui.settings.SettingsScreen
import com.example.tobaccocellar.ui.stats.StatsDestination
import com.example.tobaccocellar.ui.stats.StatsScreen


@Composable
fun CellarNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = HomeDestination.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
//                navigateToHome = { navController.navigate(HomeDestination.route) },
                navigateToStats = {
                    navController.navigate(StatsDestination.route) },
                navigateToAddEntry = { navController.navigate(AddEntryDestination.route) },
                navigateToEditEntry = { navController.navigate("${EditEntryDestination.route}/${it}") },
            )
        }
        composable(route = StatsDestination.route) {
            StatsScreen(
                navigateToHome = { navController.navigate(HomeDestination.route) {
                    popUpTo(HomeDestination.route) { inclusive = true }
                } },
//                navigateToStats = { navController.navigate(StatsDestination.route) },
                navigateToAddEntry = { navController.navigate(AddEntryDestination.route) },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(route = AddEntryDestination.route) {
            AddEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() },
                navigateToEditEntry = { navController.navigate("${EditEntryDestination.route}/${it}") }
            )
        }
        composable(
            route = EditEntryDestination.routeWithArgs,
            arguments = listOf(navArgument(EditEntryDestination.itemsIdArg) {
                type = NavType.IntType
            })
            ) {
                EditEntryScreen(
                    navigateBack = { navController.popBackStack() },
                    onNavigateUp = { navController.navigateUp() },
                )
            }
        composable(route = SettingsDestination.route) {
            SettingsScreen(
                navigateBack = { navController.navigateUp() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
