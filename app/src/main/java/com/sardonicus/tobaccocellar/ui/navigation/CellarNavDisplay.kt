package com.sardonicus.tobaccocellar.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplay.popTransitionSpec
import androidx.navigation3.ui.NavDisplay.predictivePopTransitionSpec
import androidx.navigation3.ui.NavDisplay.transitionSpec
import androidx.navigationevent.NavigationEvent
import com.sardonicus.tobaccocellar.data.ItemsRepository
import com.sardonicus.tobaccocellar.data.LocalCellarApplication
import com.sardonicus.tobaccocellar.data.PreferencesRepo
import com.sardonicus.tobaccocellar.ui.FilterViewModel
import com.sardonicus.tobaccocellar.ui.csvimport.CsvHelpScreen
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportResultsScreen
import com.sardonicus.tobaccocellar.ui.csvimport.CsvImportScreen
import com.sardonicus.tobaccocellar.ui.dates.DatesScreen
import com.sardonicus.tobaccocellar.ui.details.BlendDetailsScreen
import com.sardonicus.tobaccocellar.ui.details.BlendDetailsViewModel
import com.sardonicus.tobaccocellar.ui.home.HelpScreen
import com.sardonicus.tobaccocellar.ui.home.HomeScreen
import com.sardonicus.tobaccocellar.ui.items.AddEntryScreen
import com.sardonicus.tobaccocellar.ui.items.BulkEditScreen
import com.sardonicus.tobaccocellar.ui.items.EditEntryScreen
import com.sardonicus.tobaccocellar.ui.items.EditEntryViewModel
import com.sardonicus.tobaccocellar.ui.plaintext.PlaintextScreen
import com.sardonicus.tobaccocellar.ui.settings.ChangelogScreen
import com.sardonicus.tobaccocellar.ui.settings.SettingsScreen
import com.sardonicus.tobaccocellar.ui.stats.StatsScreen
import java.util.UUID

@Composable
fun CellarNavigation(
    navigator: Navigator,
    navigationState: NavigationState,
    modifier: Modifier = Modifier,
) {
    val filterViewModel: FilterViewModel = LocalCellarApplication.current.filterViewModel
    val preferencesRepo: PreferencesRepo = LocalCellarApplication.current.preferencesRepo
    val itemsRepository: ItemsRepository = LocalCellarApplication.current.container.itemsRepository

    val navHeight = WindowInsets.tappableElement.asPaddingValues().calculateBottomPadding()
    val isGestureNav = remember(navHeight) { navHeight == 0.dp }

    val slideTransition = transitionSpec {
        slideInHorizontally(tween(700)) { it } togetherWith ExitTransition.None
    } + popTransitionSpec {
        EnterTransition.None togetherWith slideOutHorizontally(tween(700)) { it }
    } + predictivePopTransitionSpec {
        if (isGestureNav) {
            when (it) {
                NavigationEvent.EDGE_RIGHT -> {
                    slideInHorizontally(tween(700)) { it / 2 } togetherWith
                            slideOutHorizontally(tween(700)) { -it / 2 }
                }

                NavigationEvent.EDGE_LEFT -> {
                    slideInHorizontally(tween(700)) { -it / 2 } togetherWith
                            slideOutHorizontally(tween(700)) { it / 2 }
                }

                else -> EnterTransition.None togetherWith slideOutHorizontally(tween(700)) { it }
            }
        } else {
            EnterTransition.None togetherWith slideOutHorizontally(tween(700)) { it }
        }
    }

    val entryProvider: (NavKey) -> NavEntry<NavKey> = { key ->
        when (key) {
            is HomeDestination -> NavEntry(key) {
                HomeScreen (
                    navigateToStats = { navigator.navigate(StatsDestination) },
                    navigateToDates = { navigator.navigate(DatesDestination) },
                    navigateToAddEntry = { navigator.navigate(AddEntryDestination) },
                    navigateToEditEntry = { navigator.navigate(EditEntryDestination(it)) },
                    navigateToBulkEdit = { navigator.navigate(BulkEditDestination) },
                    navigateToBlendDetails = { navigator.navigate(BlendDetailsDestination(it)) },
                    navigateToCsvImport = { navigator.navigate(CsvFlowDestination) },
                    navigateToSettings = { navigator.navigate(SettingsDestination) },
                    navigateToHelp = { navigator.navigate(HelpDestination) },
                    navigateToPlaintext = { navigator.navigate(PlaintextDestination) },
                    filterViewModel = filterViewModel,
                )
            }

            is BlendDetailsDestination -> NavEntry(key, metadata = slideTransition) {
                val viewModel: BlendDetailsViewModel = viewModel(
                    key = key.itemsId.toString(),
                    factory = viewModelFactory {
                        initializer {
                            BlendDetailsViewModel(
                                itemsId = key.itemsId,
                                itemsRepository = itemsRepository,
                                preferencesRepo = preferencesRepo
                            )
                        }
                    }
                )

                BlendDetailsScreen(
                    navigateToEditEntry = { navigator.navigate(EditEntryDestination(it)) },
                    onNavigateUp = { navigator.goBack() },
                    viewModel = viewModel
                )
            }

            is HelpDestination -> NavEntry(key) {
                HelpScreen(
                    onNavigateUp = { navigator.goBack() },
                )
            }

            is StatsDestination -> NavEntry(key) {
                StatsScreen(
                    navigateToHome = { navigator.navigate(HomeDestination) },
                    navigateToDates = { navigator.navigate(DatesDestination) },
                    navigateToAddEntry = { navigator.navigate(AddEntryDestination) },
                    modifier = Modifier
                )
            }

            is DatesDestination -> NavEntry(key) {
                DatesScreen(
                    navigateToHome = { navigator.navigate(HomeDestination) },
                    navigateToStats = { navigator.navigate(StatsDestination) },
                    navigateToAddEntry = { navigator.navigate(AddEntryDestination) },
                    navigateToDetails = { navigator.navigate(BlendDetailsDestination(it)) },
                )
            }

            is AddEntryDestination -> NavEntry(key) {
                AddEntryScreen(
                    navigateBack = { navigator.goBack() },
                    onNavigateUp = { navigator.goBack() },
                    navigateToEditEntry = { navigator.navigate(EditEntryDestination(it)) },
                )
            }

            is EditEntryDestination -> NavEntry(key) {
                val viewModel: EditEntryViewModel = viewModel(
                    key = key.itemsId.toString(),
                    factory = viewModelFactory {
                        initializer {
                            EditEntryViewModel(
                                itemsId = key.itemsId,
                                filterViewModel = filterViewModel,
                                itemsRepository = itemsRepository,
                                preferencesRepo = preferencesRepo
                            )
                        }
                    }
                )

                EditEntryScreen(
                    navigateBack = { navigator.goBack() },
                    onNavigateUp = { navigator.goBack() },
                    viewModel = viewModel
                )
            }

            is BulkEditDestination -> NavEntry(key) {
                BulkEditScreen(
                    onNavigateUp = { navigator.goBack() },
                )
            }

            is SettingsDestination -> NavEntry(key) {
                SettingsScreen(
                    onNavigateUp = { navigator.goBack() },
                    navigateToChangelog = { navigator.navigate(ChangelogDestination(it)) },
                )
            }

            is ChangelogDestination -> NavEntry(key, metadata = slideTransition) {
                ChangelogScreen(
                    onNavigateUp = { navigator.goBack() },
                    changelogEntries = key.changelogEntries
                )
            }

            is PlaintextDestination -> NavEntry(key) {
                PlaintextScreen(
                    onNavigateUp = { navigator.goBack() },
                )
            }

            is CsvFlowDestination -> NavEntry(key) {
                val csvHelpScrollState = rememberScrollState()

                val startRoute = remember { CsvImportDestination(UUID.randomUUID().toString()) }
                val nestedNavigationState = rememberNavigationState(
                    topLevelRoutes = setOf(startRoute), startRoute = startRoute
                )
                val nestedNavigator = remember { Navigator(nestedNavigationState) }

                val nestedEntryProvider: (NavKey) -> NavEntry<NavKey> = { nestedKey ->
                    when (val csvKey = nestedKey as CsvFlowKey) {
                        is CsvImportDestination -> NavEntry(csvKey) {
                            CsvImportScreen(
                                navKey = csvKey,
                                onNavigateUp = { navigator.goBack() },
                                navigateToHome = { navigator.navigate(HomeDestination) },
                                navigateToCsvHelp = { nestedNavigator.navigate(CsvHelpDestination) },
                                navigateToImportResults = { totalRecords, successCount, successfulInsertions, successfulUpdates, successfulTins, updateFlag, tinFlag ->
                                    navigator.goBack()
                                    navigator.navigate(
                                        CsvImportResultsDestination(
                                            totalRecords,
                                            successCount,
                                            successfulInsertions,
                                            successfulUpdates,
                                            successfulTins,
                                            updateFlag,
                                            tinFlag
                                        )
                                    )
                                }
                            )
                        }

                        is CsvHelpDestination -> NavEntry(csvKey, metadata = slideTransition) {
                            CsvHelpScreen(
                                onNavigateUp = { nestedNavigator.goBack() },
                                scrollState = csvHelpScrollState,
                            )
                        }
                    }
                }
                NavDisplay(
                    entries = nestedNavigationState.toEntries(nestedEntryProvider),
                    onBack = {
                        if (nestedNavigationState.canGoBack) {
                            nestedNavigator.goBack()
                        } else {
                            navigator.goBack()
                        }
                    }
                )
            }

            is CsvImportResultsDestination -> {
                val transition = transitionSpec { EnterTransition.None togetherWith fadeOut(tween(700)) }

                NavEntry(key, metadata = transition) {
                    CsvImportResultsScreen(
                        totalRecords = key.totalRecords,
                        successfulConversions = key.successCount,
                        successfulInsertions = key.successfulInsertions,
                        successfulUpdates = key.successfulUpdates,
                        successfulTins = key.successfulTins,
                        updateFlag = key.updateFlag,
                        tinFlag = key.tinFlag,
                        navigateToHome = { navigator.goBack() },
                        onNavigateUp = { navigator.goBack() }
                    )
                }
            }

            else -> error("Unknown destination: $key")
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        modifier = modifier,
        onBack = { navigator.goBack() },
        transitionSpec = { fadeIn(tween(700)) togetherWith fadeOut(tween(700)) },
        popTransitionSpec = { fadeIn(tween(700)) togetherWith fadeOut(tween(700)) },
        predictivePopTransitionSpec = {
            if (isGestureNav) {
                fadeIn() togetherWith scaleOut(targetScale = 0.7f)
            } else fadeIn(tween(700)) togetherWith fadeOut(tween(700))
        }
    )
}