package com.sardonicus.tobaccocellar.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SinglePaneSceneStrategy
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
import com.sardonicus.tobaccocellar.ui.home.FilterPane
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
    isGestureNav: Boolean,
    largeScreen: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentFrom = if (navigationState.cameFrom != null) navigationState.cameFrom!!::class.simpleName else null

    val filterViewModel: FilterViewModel = LocalCellarApplication.current.filterViewModel
    val preferencesRepo: PreferencesRepo = LocalCellarApplication.current.preferencesRepo
    val itemsRepository: ItemsRepository = LocalCellarApplication.current.container.itemsRepository

    val entryProvider: (NavKey) -> NavEntry<NavKey> = { key ->
        val paneInfo = (key as? PaneInfo)?.paneType?.let { mapOf(TwoPaneScene.PANE_TYPE to it) } ?: emptyMap()

        val slideTransition = if (!largeScreen || (key is PaneInfo && key.paneType == PaneType.NONE)) {
            transitionSpec {
                slideInHorizontally(tween(500)) { it } togetherWith ExitTransition.None
            } + popTransitionSpec {
                EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it }
            } + predictivePopTransitionSpec { swipeEdge ->
                if (isGestureNav) {
                    when (swipeEdge) {
                        NavigationEvent.EDGE_RIGHT -> {
                            slideInHorizontally(tween(500)) { it / 2 } togetherWith
                                    slideOutHorizontally(tween(500)) { -it / 2 }
                        }

                        NavigationEvent.EDGE_LEFT -> {
                            slideInHorizontally(tween(500)) { -it / 2 } togetherWith
                                    slideOutHorizontally(tween(500)) { it / 2 }
                        }

                        else -> EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it }
                    }
                } else {
                    EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it }
                }
            }
        } else emptyMap()

        val twoPaneSlide: Map<String, ContentTransform> = mapOf(
            TwoPaneScene.PANE_ENTER to
                    if (currentFrom == "BlendDetailsDestination") {
                        slideInHorizontally(tween(500)) { it } togetherWith slideOutHorizontally(tween(500)) { -it }
                    } else {
                        slideInHorizontally(tween(500)) { it } togetherWith ExitTransition.None
                    },
            TwoPaneScene.PANE_EXIT to (EnterTransition.None togetherWith slideOutHorizontally(tween(500)) { it })
        )

        when (key) {
            is HomeDestination -> NavEntry(key, metadata = paneInfo) {
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

            is BlendDetailsDestination -> NavEntry(key, metadata = slideTransition + twoPaneSlide + paneInfo) {
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

            is HelpDestination -> NavEntry(key, metadata = paneInfo) {
                HelpScreen(
                    onNavigateUp = { navigator.goBack() },
                )
            }

            is StatsDestination -> NavEntry(key, metadata = paneInfo) {
                StatsScreen(
                    navigateToHome = { navigator.navigate(HomeDestination) },
                    navigateToDates = { navigator.navigate(DatesDestination) },
                    navigateToAddEntry = { navigator.navigate(AddEntryDestination) },
                    modifier = Modifier
                )
            }

            is DatesDestination -> NavEntry(key, metadata = paneInfo) {
                DatesScreen(
                    navigateToHome = { navigator.navigate(HomeDestination) },
                    navigateToStats = { navigator.navigate(StatsDestination) },
                    navigateToAddEntry = { navigator.navigate(AddEntryDestination) },
                    navigateToDetails = { navigator.navigate(BlendDetailsDestination(it)) },
                )
            }

            is FilterPaneDestination -> NavEntry(key, metadata = paneInfo) {
                FilterPane(
                    filterViewModel = filterViewModel,
                    modifier = Modifier
                )
            }

            is AddEntryDestination -> NavEntry(key, metadata = paneInfo) {
                AddEntryScreen(
                    navigateBack = { navigator.goBack() },
                    onNavigateUp = { navigator.goBack() },
                    navigateToEditEntry = { navigator.navigate(EditEntryDestination(it)) },
                )
            }

            is EditEntryDestination -> NavEntry(key, metadata = paneInfo) {
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

            is BulkEditDestination -> NavEntry(key, metadata = paneInfo) {
                BulkEditScreen(
                    onNavigateUp = { navigator.goBack() },
                )
            }

            is SettingsDestination -> NavEntry(key, metadata = paneInfo) {
                SettingsScreen(
                    onNavigateUp = { navigator.goBack() },
                    navigateToChangelog = { navigator.navigate(ChangelogDestination(it)) },
                )
            }

            is ChangelogDestination -> NavEntry(key, metadata = slideTransition + twoPaneSlide + paneInfo) {
                ChangelogScreen(
                    onNavigateUp = { navigator.goBack() },
                    changelogEntries = key.changelogEntries
                )
            }

            is PlaintextDestination -> NavEntry(key, metadata = paneInfo) {
                PlaintextScreen(
                    onNavigateUp = { navigator.goBack() },
                )
            }

            is CsvFlowDestination -> NavEntry(key) {
                val csvHelpScrollState = rememberScrollState()

                val startRoute = remember { CsvImportDestination(UUID.randomUUID().toString()) }
                val nestedNavigationState = rememberNavigationState(startRoute, setOf(startRoute))
                val nestedNavigator = remember { Navigator(nestedNavigationState) }

                val nestedEntryProvider: (NavKey) -> NavEntry<NavKey> = { nestedKey ->
                    when (val csvKey = nestedKey as CsvFlowKey) {

                        is CsvImportDestination -> NavEntry(csvKey, metadata = paneInfo) {
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

                        is CsvHelpDestination -> NavEntry(csvKey, metadata = slideTransition + paneInfo) {
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
                val transition = transitionSpec { EnterTransition.None togetherWith fadeOut(tween(500)) }

                NavEntry(key, metadata = transition + paneInfo) {
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

    val currentTop = navigationState.topLevelRoute.let { it is PaneInfo && it.paneType == PaneType.MAIN }
    val currentLast = navigationState.currentStack.lastOrNull().let { it is PaneInfo && it.paneType == PaneType.SECOND }
    val twoPaneCompatible: Boolean = remember(largeScreen, currentTop, currentLast) { largeScreen && currentTop && currentLast }
    val scene = if (twoPaneCompatible) rememberTwoPaneStrategy<NavKey>(navigationState.twoPaneSceneKey.intValue, navigationState.interceptBack)
        else remember { SinglePaneSceneStrategy() }


    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        modifier = modifier,
        onBack = { navigator.goBack() },
        sceneStrategy = scene,
        transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
        popTransitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
        predictivePopTransitionSpec = {
            if (isGestureNav && (scene::class != TwoPaneStrategy::class)) {
                fadeIn() togetherWith scaleOut(targetScale = 0.7f)
            } else fadeIn(tween(500)) togetherWith fadeOut(tween(500))
        }

//        transitionSpec = {
//            if (targetState is TwoPaneScene<*> && initialState is TwoPaneScene<*>) {
//                EnterTransition.None togetherWith ExitTransition.None
//            } else fadeIn(tween(500)) togetherWith fadeOut(tween(500))
//        },
//        popTransitionSpec = {
//            if (targetState is TwoPaneScene<*> && initialState is TwoPaneScene<*>) {
//                EnterTransition.None togetherWith ExitTransition.None
//            } else fadeIn(tween(500)) togetherWith fadeOut(tween(500))
//        },
//        predictivePopTransitionSpec = {
//            if (targetState is TwoPaneScene<*> && initialState is TwoPaneScene<*>) {
//                EnterTransition.None togetherWith ExitTransition.None
//            } else
//                if (isGestureNav && (scene::class != TwoPaneStrategy::class)) {
//                    fadeIn() togetherWith scaleOut(targetScale = 0.7f)
//                } else fadeIn(tween(500)) togetherWith fadeOut(tween(500))
//        }
    )
}