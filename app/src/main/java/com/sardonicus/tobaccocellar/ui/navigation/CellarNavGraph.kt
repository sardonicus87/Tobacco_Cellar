package com.sardonicus.tobaccocellar.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.sardonicus.tobaccocellar.ui.home.BlendDetailsDestination
import com.sardonicus.tobaccocellar.ui.home.BlendDetailsScreen
import com.sardonicus.tobaccocellar.ui.home.HelpDestination
import com.sardonicus.tobaccocellar.ui.home.HelpScreen
import com.sardonicus.tobaccocellar.ui.home.HomeDestination
import com.sardonicus.tobaccocellar.ui.home.HomeScreen
import com.sardonicus.tobaccocellar.ui.items.AddEntryDestination
import com.sardonicus.tobaccocellar.ui.items.AddEntryScreen
import com.sardonicus.tobaccocellar.ui.items.BulkEditDestination
import com.sardonicus.tobaccocellar.ui.items.BulkEditScreen
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
        composable(
            route = HomeDestination.route,
            enterTransition = {
                if (initialState.destination.route == BlendDetailsDestination.routeWithArgs) {
                    EnterTransition.None } else { null }
            },
            popEnterTransition = {
                if (initialState.destination.route == BlendDetailsDestination.routeWithArgs) {
                    EnterTransition.None } else { null }
            }
        ) {
            HomeScreen(
                navigateToStats = { navController.navigate(StatsDestination.route) {
                    launchSingleTop = true
                    popUpTo(HomeDestination.route) { inclusive = false }
                } },
                navigateToAddEntry = { navController.navigate(AddEntryDestination.route) },
                navigateToEditEntry = { navController.navigate("${EditEntryDestination.route}/${it}") },
                navigateToBulkEdit = { navController.navigate(BulkEditDestination.route) },
                navigateToBlendDetails = { navController.navigate("${BlendDetailsDestination.route}/${it}") },
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
        composable(
            route = BlendDetailsDestination.routeWithArgs,
            arguments = listOf(navArgument(BlendDetailsDestination.itemsIdArg) { type = NavType.IntType }),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            BlendDetailsScreen(
                navigateBack = { navController.navigate(HomeDestination.route) {
                    launchSingleTop = true
                    popUpTo(HomeDestination.route) { inclusive = false }
                }  },
                onNavigateUp = { navController.navigateUp() },
            )
        }
        composable(route = BulkEditDestination.route) {
            BulkEditScreen(
                navigateBack = { navController.navigateUp() },
                onNavigateUp = { navController.navigateUp() },
            )
        }

        composable(route = CsvImportDestination.route) {
            CsvImportScreen(
                navigateBack = { navController.navigateUp() },
                onNavigateUp = { navController.navigateUp() },
                navigateToHome = { navController.navigate(HomeDestination.route) },
                navigateToImportResults = { totalRecords, successCount, successfulInsertions, successfulUpdates, successfulTins, updateFlag, tinFlag -> navController.navigate(
                    "${CsvImportResultsDestination.route}/${totalRecords}/${successCount}/${successfulInsertions}/${successfulUpdates}/${successfulTins}/${updateFlag}/${tinFlag}")
                },
            )
        }
        composable(
            route = CsvImportResultsDestination.routeWithArgs,
            enterTransition = { EnterTransition.None },
            arguments = listOf(
                navArgument(CsvImportResultsDestination.totalRecordsArg) { type = NavType.IntType },
                navArgument(CsvImportResultsDestination.successCountArg) { type = NavType.IntType },
                navArgument(CsvImportResultsDestination.successfulInsertionsArg) { type = NavType.IntType },
                navArgument(CsvImportResultsDestination.successfulUpdatesArg) { type = NavType.IntType },
                navArgument(CsvImportResultsDestination.successfulTinsArg) { type = NavType.IntType },
                navArgument(CsvImportResultsDestination.updateFlagArg) { type = NavType.BoolType },
                navArgument(CsvImportResultsDestination.tinFlagArg) { type = NavType.BoolType },
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
            val successfulTins =
                backStackEntry.arguments?.getInt(CsvImportResultsDestination.successfulTinsArg) ?: 0
            val updateFlag =
                backStackEntry.arguments?.getBoolean(CsvImportResultsDestination.updateFlagArg) ?: false
            val tinFlag =
                backStackEntry.arguments?.getBoolean(CsvImportResultsDestination.tinFlagArg) ?: false

            CsvImportResultsScreen(
                totalRecords = totalRecords,
                successfulConversions = successCount,
                successfulInsertions = successfulInsertions,
                successfulUpdates = successfulUpdates,
                successfulTins = successfulTins,
                updateFlag = updateFlag,
                tinFlag = tinFlag,
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