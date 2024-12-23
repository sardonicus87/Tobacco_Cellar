package com.sardonicus.tobaccocellar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportDestination
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportResultsDestination
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportResultsScreen
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportScreen
import com.sardonicus.tobaccocellar.ui.home.HomeDestination
import com.sardonicus.tobaccocellar.ui.home.HomeScreen
import com.sardonicus.tobaccocellar.ui.home.HelpDestination
import com.sardonicus.tobaccocellar.ui.home.HelpScreen
import com.sardonicus.tobaccocellar.ui.items.AddEntryDestination
import com.sardonicus.tobaccocellar.ui.items.AddEntryScreen
import com.sardonicus.tobaccocellar.ui.items.EditEntryDestination
import com.sardonicus.tobaccocellar.ui.items.EditEntryScreen
import com.sardonicus.tobaccocellar.ui.settings.SettingsDestination
import com.sardonicus.tobaccocellar.ui.settings.SettingsScreen
import com.sardonicus.tobaccocellar.ui.stats.StatsDestination
import com.sardonicus.tobaccocellar.ui.stats.StatsScreen


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
                navigateToHelp = { navController.navigate(HelpDestination.route) {
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
        composable(route = HelpDestination.route) {
            HelpScreen(
                navigateBack = { navController.popBackStack() },
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
                navigateToImportResults = { totalRecords, successCount, successfulInsertions, successfulUpdates -> navController.navigate(
                    "${CsvImportResultsDestination.route}/${totalRecords}/${successCount}/${successfulInsertions}/${successfulUpdates}")
                },
            )
        }
        composable(
            route = CsvImportResultsDestination.routeWithArgs,
            arguments = listOf(
                navArgument(CsvImportResultsDestination.totalRecordsArg) { type = NavType.IntType },
                navArgument(CsvImportResultsDestination.successCountArg) { type = NavType.IntType },
                navArgument(CsvImportResultsDestination.successfulInsertionsArg) { type = NavType.IntType },
                navArgument(CsvImportResultsDestination.successfulUpdatesArg) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val totalRecords =
                backStackEntry.arguments?.getInt(CsvImportResultsDestination.totalRecordsArg) ?: 0
            val successCount =
                backStackEntry.arguments?.getInt(CsvImportResultsDestination.successCountArg) ?: 0
            val successfulInsertions =
                backStackEntry.arguments?.getInt(CsvImportResultsDestination.successfulInsertionsArg) ?: 0
            val successfulUpdates =
                backStackEntry.arguments?.getInt(CsvImportResultsDestination.successfulUpdatesArg) ?: 0
            CsvImportResultsScreen(
                totalRecords = totalRecords,
                successfulConversions = successCount,
                successfulInsertions = successfulInsertions,
                successfulUpdates = successfulUpdates,
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