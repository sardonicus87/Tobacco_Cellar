package com.example.tobaccocellar.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tobaccocellar.ui.csvimport.CsvImportDestination
import com.example.tobaccocellar.ui.csvimport.CsvImportResultsDestination
import com.example.tobaccocellar.ui.csvimport.CsvImportResultsScreen
import com.example.tobaccocellar.ui.csvimport.CsvImportScreen
import com.example.tobaccocellar.ui.home.HomeDestination
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
                navigateToStats = { navController.navigate(StatsDestination.route) {
                    launchSingleTop = true
                    popUpTo(HomeDestination.route) { inclusive = false }
                } },
                navigateToAddEntry = { navController.navigate(AddEntryDestination.route) },
                navigateToEditEntry = { navController.navigate("${EditEntryDestination.route}/${it}") },
                navigateToCsvImport = { navController.navigate(CsvImportDestination.route) },
                navigateToSettings = { navController.navigate(SettingsDestination.route) {
                    launchSingleTop = true
                } },
            )
        }
        composable(route = StatsDestination.route) {
            StatsScreen(
                navigateToHome = { navController.navigate(HomeDestination.route) {
                    launchSingleTop = true
                    popUpTo(StatsDestination.route) { inclusive = true }
                } },
                navigateToAddEntry = { navController.navigate(AddEntryDestination.route) },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(route = SettingsDestination.route) {
            SettingsScreen(
                navigateBack = { navController.navigateUp() },
                onNavigateUp = { navController.navigateUp() },
            )
        }
        composable(route = AddEntryDestination.route) {
            AddEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() },
                navigateToEditEntry = { navController.navigate("${EditEntryDestination.route}/${it}") },
            )
        }
        composable(
            route = EditEntryDestination.routeWithArgs,
            arguments = listOf(navArgument(EditEntryDestination.itemsIdArg) {
                type = NavType.IntType
            })
        ) {
            EditEntryScreen(
                navigateBack = { navController.navigate(HomeDestination.route) {
                    launchSingleTop = true
                    popUpTo(HomeDestination.route) { inclusive = true }
                } },
                onNavigateUp = { navController.navigateUp() },
            )
        }
        composable(route = CsvImportDestination.route) {
            CsvImportScreen(
                navigateBack = { navController.navigateUp() },
                onNavigateUp = { navController.navigateUp() },
                navigateToHome = { navController.navigate(HomeDestination.route) },
                navigateToImportResults = { totalRecords, successCount, successfulInsertions -> navController.navigate(
                    "${CsvImportResultsDestination.route}/${totalRecords}/${successCount}/${successfulInsertions}")
                }
            )
        }
        composable(
            route = CsvImportResultsDestination.routeWithArgs,
            arguments = listOf(
                navArgument(CsvImportResultsDestination.totalRecordsArg) { type = NavType.IntType },
                navArgument(CsvImportResultsDestination.successCountArg) { type = NavType.IntType },
                navArgument(CsvImportResultsDestination.successfulInsertionsArg) { type = NavType.IntType }
            ),
        ) { backStackEntry ->
            val totalRecords =
                backStackEntry.arguments?.getInt(CsvImportResultsDestination.totalRecordsArg) ?: 0
            val successCount =
                backStackEntry.arguments?.getInt(CsvImportResultsDestination.successCountArg) ?: 0
            val successfulInsertions =
                backStackEntry.arguments?.getInt(CsvImportResultsDestination.successfulInsertionsArg) ?: 0
            CsvImportResultsScreen(
                totalRecords = totalRecords,
                successfulConversions = successCount,
                successfulInsertions = successfulInsertions,
                navigateToHome = { navController.navigate(HomeDestination.route) {
                    launchSingleTop = true
                    popUpTo(HomeDestination.route) { inclusive = false }
                } },
                navigateBack = { navController.navigate(HomeDestination.route) },
                onNavigateUp = { navController.navigate(HomeDestination.route) }
            )
        }
    }
}